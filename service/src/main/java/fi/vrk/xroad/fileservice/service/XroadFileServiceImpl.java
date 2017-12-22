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
        if ( name == null || "".equals(name) ) {
            throw new ErrorResponse("Expected a file name");
        }

        Path file = Paths.get(outgoingDirectory,
                Paths.get("/", name).normalize().toString());

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
                clientId = identifiers
                        .createUnmarshaller()
                        .unmarshal((Node) client.getObject(), XRoadClientIdentifierType.class).getValue();
            } catch (JAXBException e) {
                throw new ErrorResponse("Unable to unmarshal client id", e);
            }
        }
        return clientId;
    }

    @SuppressWarnings("unchecked")
    private Map<String, Header> getXroadHeaders(MessageContext ctx) {
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
