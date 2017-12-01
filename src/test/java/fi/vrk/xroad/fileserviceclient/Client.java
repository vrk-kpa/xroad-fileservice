package fi.vrk.xroad.fileserviceclient;

import eu.x_road.xsd.identifiers.XRoadClientIdentifierType;
import eu.x_road.xsd.identifiers.XRoadObjectType;
import eu.x_road.xsd.identifiers.XRoadServiceIdentifierType;
import fi.vrk.xroad.fileservice.ErrorResponse;

import javax.activation.DataHandler;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.Holder;

import java.io.InputStream;
import java.util.UUID;

public class Client {

    private final XRoadClientIdentifierType clientId;
    private final XRoadServiceIdentifierType serviceId;
    private final XroadFileService port;

    public Client(String url, String client, String service) {
        XroadFileService_Service ss = new XroadFileService_Service();
        port = ss.getXroadFileServicePort();
        BindingProvider bindingProvider = (BindingProvider) port;
        bindingProvider.getRequestContext().put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, url);

        final String[] c = client.split("/");
        clientId = new XRoadClientIdentifierType();
        clientId.setObjectType(XRoadObjectType.SUBSYSTEM);
        clientId.setXRoadInstance(c[0]);
        clientId.setMemberClass(c[1]);
        clientId.setMemberCode(c[2]);
        clientId.setSubsystemCode(c[3]);

        final String[] s = service.split("/");
        serviceId = new XRoadServiceIdentifierType();
        serviceId.setObjectType(XRoadObjectType.SERVICE);
        serviceId.setXRoadInstance(s[0]);
        serviceId.setMemberClass(s[1]);
        serviceId.setMemberCode(s[2]);
        serviceId.setSubsystemCode(s[3]);
        serviceId.setServiceCode("get");
    }

    public DataHandler get(String fileName) throws ErrorResponse {
        return port.get(
                fileName,
                holder(clientId),
                holder(serviceId),
                holder("fileserviceclient"),
                holder(UUID.randomUUID().toString()),
                holder("4.0"));
    }

    private static <T> Holder<T> holder(T value) {
        return new Holder<>(value);
    }

    /**
     * Stand-alone test client
     *
     * @param args url clientId serviceId filename
     */
    public static void main(String[] args) {
        try {
            Client client = new Client(args[0], args[1], args[2]);
            DataHandler result = client.get(args[3]);
            try (InputStream ios = result.getInputStream()) {
                byte[] buf = new byte[4096];
                int c;
                while ((c = ios.read(buf)) > 0) {
                    System.out.write(buf, 0, c);
                }
            }
        } catch (Exception e) {
            System.out.println(e.toString());
        }
    }
}
