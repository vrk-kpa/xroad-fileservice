package fi.vrk.xroad.fileservice.service;

import fi.vrk.xroad.fileservice.client.Client;
import org.apache.commons.io.IOUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.context.embedded.LocalServerPort;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.activation.DataHandler;
import javax.xml.ws.Holder;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static org.junit.Assert.assertTrue;

@RunWith(SpringRunner.class)
@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {"server.address=127.0.0.1", "outgoing-directory=./build"},
        classes = Application.class
)
public class XroadFileServiceTest {

    @LocalServerPort
    int port;

    @Test
    public void shouldDownloadTestFile() throws Exception {

        final Client client = new Client("http://127.0.0.1:" + port + "/fileservice",
                "INSTANCE/MEMBER/CLIENT/SUBSYSTEM",
                "INSTANCE/MEMBER/SERVICE/SUBSYSTEM");

        DataHandler result = client.get("/resources/test/test.txt");
        final ByteArrayOutputStream buf = new ByteArrayOutputStream();
        try (InputStream ios = result.getInputStream()) {
            IOUtils.copy(ios, buf);
        }
        assertTrue(new String(buf.toByteArray(), StandardCharsets.UTF_8).startsWith("TEST"));
    }

    private static <T> Holder<T> holder(T value) {
        return new Holder<>(value);
    }
}

