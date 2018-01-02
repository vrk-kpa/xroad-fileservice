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
import javax.activation.DataSource;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.Holder;
import javax.xml.ws.soap.SOAPBinding;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.List;
import java.util.UUID;

/**
 * Xroad FileService Client
 */
public class Client {

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
        ((SOAPBinding)bindingProvider.getBinding()).setMTOMEnabled(true);

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

    /**
     * Fetch a file.
     */
    public DataHandler get(String fileName) throws ErrorResponse {
        return port.get(fileName,
                holder(clientId),
                holder(serviceId.build("get")),
                holder("fileserviceclient"),
                holder(UUID.randomUUID().toString()), holder("4.0"));
    }

    /**
     * List available files
     */
    public List<String> list() throws ErrorResponse {
        return port.list(holder(clientId),
                holder(serviceId.build("get")),
                holder("fileserviceclient"),
                holder(UUID.randomUUID().toString()), holder("4.0"));
    }

    /**
     * Upload a file
     */
    public boolean put(String fileName, InputStream input) throws ErrorResponse {
        return port.put(fileName,
                new DataHandler(new StreamDataSource(fileName, input)),
                holder(clientId),
                holder(serviceId.build("get")),
                holder("fileserviceclient"),
                holder(UUID.randomUUID().toString()), holder("4.0"));
    }

    private static <T> Holder<T> holder(T value) {
        return new Holder<>(value);
    }

    static class StreamDataSource implements DataSource {

        private final String name;
        private final InputStream input;

        StreamDataSource(String name, InputStream input) {
            this.name = name;
            this.input = input;
        }

        @Override
        public InputStream getInputStream() throws IOException {
            return input;
        }

        @Override
        public OutputStream getOutputStream() throws IOException {
            return null;
        }

        @Override
        public String getContentType() {
            return "application/octet-stream";
        }

        @Override
        public String getName() {
            return name;
        }
    }

}
