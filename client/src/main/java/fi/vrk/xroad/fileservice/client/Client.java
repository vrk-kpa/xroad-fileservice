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
package fi.vrk.xroad.fileservice.client;

import fi.vrk.xroad.fileservice.ErrorResponse;

import eu.x_road.xsd.identifiers.XRoadClientIdentifierType;
import eu.x_road.xsd.identifiers.XRoadObjectType;
import org.apache.cxf.frontend.ClientProxy;
import org.apache.cxf.transport.http.HTTPConduit;

import javax.activation.DataHandler;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.Holder;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

/**
 * Xroad FileService Client
 */
public class Client {

    private static final Logger LOG = Logger.getLogger(Client.class.getName());

    private final XRoadClientIdentifierType clientId;
    private final XRoadServiceIdBuilder serviceId;
    private final XroadFileService port;

    /**
     * Constructs a new Client
     */
    public Client(String url, String client, String service) {
        XroadFileService_Service ss = new XroadFileService_Service((URL) null);
        port = ss.getXroadFileServicePort();

        configureReceiveTimeout();

        BindingProvider bindingProvider = (BindingProvider) port;
        bindingProvider.getRequestContext().put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, url);

        final String[] c = client.split("/");
        if (c.length < 4) {
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
        if (c.length < 4) {
            throw new IllegalArgumentException(
                    "Expected serviceId in format instanceId/memberClass/memberCode/subsystemCode");
        }
        serviceId = new XRoadServiceIdBuilder(s[0], s[1], s[2], s[3], null);
    }

    private void configureReceiveTimeout() {
        final org.apache.cxf.endpoint.Client cxfClient = ClientProxy.getClient(port);
        ((HTTPConduit) cxfClient.getConduit()).getClient()
                .setReceiveTimeout(Integer.getInteger("fileservice.client.receiveTimeout", 0 /* infinite */));
    }

    public DataHandler get(String fileName) throws ErrorResponse {
        return port.get(fileName, holder(clientId), holder(serviceId.build("get")), holder("fileserviceclient"),
                holder(UUID.randomUUID().toString()), holder("4.0"));
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

        if (args.length < 4) {
            usage();
            System.exit(1);
        }

        try {
            Client client = new Client(args[0], args[1], args[2]);

            OutputStream out;
            if (args.length > 4) {
                final Path name = ".".equals(args[4]) ? Paths.get(args[3]).getFileName() : Paths.get(args[4]);
                out = Files.newOutputStream(name, StandardOpenOption.CREATE_NEW);
            } else {
                out = System.out;
            }

            try {
                client.get(args[3]).writeTo(out);
                out.flush();
            } finally {
                if (out != System.out) out.close();
            }

        } catch (ErrorResponse e) {
            LOG.severe(e.getMessage());
            System.exit(1);
        } catch (FileAlreadyExistsException e) {
            LOG.severe("The output file already exists.");
            System.exit(1);
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Unexpected error", e);
            System.exit(127);
        }
    }

    private static void usage() {
        System.out.println("Usage: (java -jar ...) <url> <clientId> <memberId> <filename> [outfile]\n"
                + "\turl     : service or client security server URL\n"
                + "\tclientId: instanceId/memberClass/memberCode/subsystemCode\n"
                + "\tmemberId: service memberId, same format as clientId\n" + "\tfilename: name of the file to fetch\n"
                + "\toutfile : file to write the output to (must not exist) or standard output if omitted\n");
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
