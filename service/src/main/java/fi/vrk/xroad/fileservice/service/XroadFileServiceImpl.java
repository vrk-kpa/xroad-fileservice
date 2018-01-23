/**
 * MIT License
 *
 * Copyright (c) 2017 Population Register Centre
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package fi.vrk.xroad.fileservice.service;

import fi.vrk.xroad.fileservice.ErrorResponse;
import fi.vrk.xroad.fileservice.ErrorResponseType;

import eu.x_road.xsd.identifiers.XRoadClientIdentifierType;
import lombok.extern.slf4j.Slf4j;
import org.apache.cxf.headers.Header;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.w3c.dom.Node;

import javax.activation.DataHandler;
import javax.activation.FileDataSource;
import javax.annotation.Resource;
import javax.jws.WebService;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.ws.WebServiceContext;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.soap.MTOM;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.DirectoryStream;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 * XRoad FileService Implementation
 */
@WebService(targetNamespace = "http://vrk.fi/xroad/fileservice", wsdlLocation = "fileservice.wsdl",
        portName = "XroadFileServicePort", serviceName = "XroadFileService")
@MTOM
@Slf4j
public class XroadFileServiceImpl implements XroadFileService {

    // maximum supported file name length
    private static final int MAX_FILENAME_LENGTH = 255;

    @Value("${outgoing-directory:/var/spool/xroad-fileservice/outgoing}")
    private Path outgoingDirectory;

    @Value("${incoming-directory:/var/spool/xroad-fileservice/incoming}")
    private Path incomingDirectory;

    @Resource
    private WebServiceContext ctx;

    @Autowired
    private JAXBContext identifiers;

    @Override
    public List<String> list() throws ErrorResponse {
        return handle("LIST", "/", () -> {
            try (DirectoryStream<Path> dir = Files.newDirectoryStream(outgoingDirectory)) {
                return StreamSupport
                        .stream(Spliterators.spliteratorUnknownSize(dir.iterator(), Spliterator.ORDERED), false)
                        .filter(Files::isRegularFile)
                        .map(p -> p.getFileName().toString())
                        .collect(Collectors.toList());
            } catch (IOException ioe) {
                throw new ErrorResponse("Unable to list files");
            }
        });
    }

    @Override
    public DataHandler get(final String name) throws ErrorResponse {
        return handle("GET", name, () -> {
            if (name == null || "".equals(name)) {
                throw new ErrorResponse("Expected a file name");
            }

            Path file = normalize(outgoingDirectory, name);

            if (!Files.isRegularFile(file)) {
                final ErrorResponseType detail = new ErrorResponseType();
                detail.setError("File not found: " + name);
                throw new ErrorResponse("The requested file does not exist", detail);
            }

            return new DataHandler(new FileDataSource(file.toFile()));
        });
    }

    @Override
    public boolean put(final String name, final DataHandler object) throws ErrorResponse {
        return handle("PUT", name, () -> {
            Path file = normalize(incomingDirectory, name);
            try (OutputStream out = Files.newOutputStream(file, StandardOpenOption.CREATE_NEW)) {
                object.writeTo(out);
            } catch (FileAlreadyExistsException e) {
                throw new ErrorResponse("The file already exists");
            } catch (IOException e) {
                try {
                    Files.deleteIfExists(file);
                } catch (IOException ioe) {
                    log.warn(e.getMessage());
                }
                log.warn("Failed to put file", e);
                throw new ErrorResponse("Unable to save file", e);
            }
            return true;
        });
    }

    private Path normalize(Path prefix, String fileName) throws ErrorResponse {
        if (fileName.length() > MAX_FILENAME_LENGTH) {
            throw new ErrorResponse("Invalid file name");
        }

        final Path name = prefix.resolve(fileName).normalize().getFileName();
        if (name == null || !Objects.equals(name.toString(), fileName)) {
            throw new ErrorResponse("Invalid file name");
        }

        return prefix.resolve(name);
    }

    private <T> T handle(String method, String details, ThrowingSupplier<T> supplier) throws ErrorResponse {
        final Map<String, Header> xroadHeaders = getXroadHeaders(ctx.getMessageContext());

        XRoadClientIdentifierType clientId = getClientId(xroadHeaders);
        log.info(String.format("(%s) %s \"%.256s\"", asString(clientId), method, details));

        T result = supplier.get();

        //Echo all X-Road headers back
        for (Header h : xroadHeaders.values()) {
            h.setDirection(Header.Direction.DIRECTION_INOUT);
        }

        return result;
    }

    interface ThrowingSupplier<T> {
        T get() throws ErrorResponse;
    }

    /*
    Convert the XroadClientIdentifierType to a string representation
     */
    private static String asString(XRoadClientIdentifierType clientId) {
        if (clientId != null) {
            StringBuilder b = new StringBuilder();
            b.append(clientId.getXRoadInstance());
            b.append("/");
            b.append(clientId.getMemberClass());
            b.append("/");
            b.append(clientId.getMemberCode());
            if (clientId.getSubsystemCode() != null) {
                b.append("/");
                b.append(clientId.getSubsystemCode());
            }
            return b.toString();
        }
        return "";
    }

    private XRoadClientIdentifierType getClientId(Map<String, Header> xroadHeaders) throws ErrorResponse {
        final Header client = xroadHeaders.get("client");
        XRoadClientIdentifierType clientId = null;
        if (client != null) {
            try {
                clientId = identifiers.createUnmarshaller()
                        .unmarshal((Node) client.getObject(), XRoadClientIdentifierType.class).getValue();
            } catch (JAXBException e) {
                throw new ErrorResponse("Unable to unmarshal client id", e);
            }
        }
        return clientId;
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Header> getXroadHeaders(MessageContext ctx) {
        final Map<String, Header> xroadHeaders = new HashMap<>();
        final List<Header> headers = (List<Header>) ctx.get(Header.HEADER_LIST);
        for (Header header : headers) {
            if ("http://x-road.eu/xsd/xroad.xsd".equals(header.getName().getNamespaceURI())) {
                xroadHeaders.put(header.getName().getLocalPart(), header);
            }
        }
        return xroadHeaders;
    }
}
