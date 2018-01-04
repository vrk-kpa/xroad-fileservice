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
import fi.vrk.xroad.fileservice.GetRequestType;
import fi.vrk.xroad.fileservice.GetResponseType;

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

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * XRoad FileService Implementation
 */
@WebService(targetNamespace = "http://vrk.fi/xroad/fileservice", wsdlLocation = "fileservice.wsdl",
        portName = "XroadFileServicePort", serviceName = "XroadFileService")
@MTOM
@Slf4j
public class XroadFileServiceImpl implements XroadFileService {

    @Value("${outgoing-directory:/var/spool/xroad-fileservice/outgoing}")
    private String outgoingDirectory;

    @Resource
    private WebServiceContext ctx;

    @Autowired
    private JAXBContext identifiers;

    @Override
    public GetResponseType get(GetRequestType parameters) throws ErrorResponse {

        GetResponseType response = new GetResponseType();
        final Map<String, Header> xroadHeaders = getXroadHeaders(ctx.getMessageContext());

        XRoadClientIdentifierType clientId = getClientId(xroadHeaders);
        log.info("({}) GET \"{}\"", asString(clientId), parameters.getName());

        final String name = parameters.getName();
        if (name == null || "".equals(name)) {
            throw new ErrorResponse("Expected a file name");
        }

        Path file = Paths.get(outgoingDirectory, Paths.get("/", name).normalize().toString());

        if (!Files.exists(file)) {
            final ErrorResponseType detail = new ErrorResponseType();
            detail.setError("File not found: " + name);
            throw new ErrorResponse("The requested file does not exist", detail);
        }

        response.setObject(new DataHandler(new FileDataSource(file.toFile())));

        //Echo all X-Road headers back
        for (Header h : xroadHeaders.values()) {
            h.setDirection(Header.Direction.DIRECTION_INOUT);
        }

        return response;
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
