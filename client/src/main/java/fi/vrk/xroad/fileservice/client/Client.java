package fi.vrk.xroad.fileservice.client;

import fi.vrk.xroad.fileservice.ErrorResponse;

import eu.x_road.xsd.identifiers.XRoadClientIdentifierType;
import eu.x_road.xsd.identifiers.XRoadObjectType;
import eu.x_road.xsd.identifiers.XRoadServiceIdentifierType;
import org.apache.cxf.frontend.ClientProxy;
import org.apache.cxf.transport.http.HTTPConduit;

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
    private final ServiceIdBuilder serviceId;
    private final XroadFileService port;

    public Client(String url, String client, String service) {
        XroadFileService_Service ss = new XroadFileService_Service((URL)null);
        port = ss.getXroadFileServicePort();

        configureReceiveTimeout();

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
        serviceId = new ServiceIdBuilder(s[0], s[1], s[2], s[3], null);
    }

    private void configureReceiveTimeout() {
        final org.apache.cxf.endpoint.Client cxfClient = ClientProxy.getClient(port);
        ((HTTPConduit)cxfClient.getConduit()).getClient().setReceiveTimeout(
                Integer.getInteger("fileservice.client.receiveTimeout", 0 /* infinite */));
    }

    public DataHandler get(String fileName) throws ErrorResponse {
        return port.get(
                fileName,
                holder(clientId),
                holder(serviceId.build("get")),
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

    static class ServiceIdBuilder {
        private final String xRoadInstance;
        private final String memberClass;
        private final String memberCode;
        private final String subsystemCode;
        private final String serviceVersion;

        ServiceIdBuilder(String xRoadInstance, String memberClass, String memberCode, String subsystemCode,
                String serviceVersion) {
            this.xRoadInstance = xRoadInstance;
            this.memberClass = memberClass;
            this.memberCode = memberCode;
            this.subsystemCode = subsystemCode;
            this.serviceVersion = serviceVersion;
        }

        XRoadServiceIdentifierType build(String serviceCode) {
            final XRoadServiceIdentifierType serviceId = new XRoadServiceIdentifierType();
            serviceId.setObjectType(XRoadObjectType.SERVICE);
            serviceId.setXRoadInstance(xRoadInstance);
            serviceId.setMemberClass(memberClass);
            serviceId.setMemberCode(memberCode);
            serviceId.setSubsystemCode(subsystemCode);
            serviceId.setServiceVersion(serviceVersion);
            serviceId.setServiceCode(serviceCode);
            return serviceId;
        }
    }

    static {
        if (System.getProperty("java.util.logging.config.file") == null) {
            // read default configuration from class path
            try (InputStream is = Client.class.getResourceAsStream("/logging.properties")) {
                LogManager.getLogManager().readConfiguration(is);
            } catch (IOException e) {
                //ignore
            }
        }
    }
}
