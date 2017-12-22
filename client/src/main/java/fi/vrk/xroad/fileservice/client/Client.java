package fi.vrk.xroad.fileservice.client;

import fi.vrk.xroad.fileservice.ErrorResponse;

import eu.x_road.xsd.identifiers.XRoadClientIdentifierType;
import eu.x_road.xsd.identifiers.XRoadObjectType;
import eu.x_road.xsd.identifiers.XRoadServiceIdentifierType;

import javax.activation.DataHandler;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.Holder;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.UUID;
import java.util.logging.LogManager;
import java.util.logging.Logger;

public class Client {

    private final XRoadClientIdentifierType clientId;
    private final XRoadServiceIdentifierType serviceId;
    private final XroadFileService port;

    public Client(String url, String client, String service) {
        XroadFileService_Service ss = new XroadFileService_Service((URL)null);
        port = ss.getXroadFileServicePort();
        BindingProvider bindingProvider = (BindingProvider) port;
        bindingProvider.getRequestContext().put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, url);

        final String[] c = client.split("/");
        if ( c.length < 4 ) {
            throw new IllegalArgumentException(
                    "Expected clientId in format instanceId/memberClass/memberCode/subsystemCode");
        }
        clientId = new XRoadClientIdentifierType();
        clientId.setObjectType(XRoadObjectType.SUBSYSTEM);
        clientId.setXRoadInstance(c[0]);
        clientId.setMemberClass(c[1]);
        clientId.setMemberCode(c[2]);
        clientId.setSubsystemCode(c[3]);

        final String[] s = service.split("/");
        if ( c.length < 4 ) {
            throw new IllegalArgumentException(
                    "Expected serviceId in format instanceId/memberClass/memberCode/subsystemCode");
        }
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
     * Stand-alone fileservice client
     *
     * @param args url clientId serviceId filename
     */
    public static void main(String[] args) {

        if ( args.length < 4 ) {
            usage();
            System.exit(1);
        }

        try {
            Client client = new Client(args[0], args[1], args[2]);
            DataHandler result = client.get(args[3]);
            OutputStream out;

            if ( args.length > 4 ) {
                final Path name = ".".equals(args[4]) ? Paths.get(args[3]).getFileName() : Paths.get(args[4]);
                out = Files.newOutputStream(name, StandardOpenOption.CREATE_NEW);
            } else {
                out = System.out;
            }

            try (InputStream ios = result.getInputStream()) {
                byte[] buf = new byte[8*1024];
                int c;
                while ((c = ios.read(buf)) > 0) {
                    out.write(buf, 0, c);
                }
                out.flush();
            } finally {
                if ( out != System.out ) out.close();
            }

        } catch (Exception e) {
            Logger.getLogger(Client.class.getName()).severe(e.getMessage());
            System.exit(1);
        }
    }

    private static void usage() {
        System.out.println(
                "Usage: (java -jar ...) <url> <clientId> <memberId> <filename> [outfile]\n"
                + "\turl     : service or client security server URL\n"
                + "\tclientId: instanceId/memberClass/memberCode/subsystemCode\n"
                + "\tmemberId: service memberId, same format as clientId\n"
                + "\tfilename: name of the file to fetch\n"
                + "\toutfile : file to write the output to (must not exist) or standard output if omitted\n");
    }

    static {
        try (InputStream is = Client.class.getResourceAsStream("/logging.properties")) {
            LogManager.getLogManager().readConfiguration(is);
        } catch (IOException e) {
            //Ignore
        }
    }
}
