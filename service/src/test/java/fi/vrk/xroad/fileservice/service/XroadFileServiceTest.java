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
import fi.vrk.xroad.fileservice.client.Client;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.context.embedded.LocalServerPort;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.activation.DataHandler;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;

import static org.junit.Assert.assertTrue;

/**
 * XRoad File Service Integration test
 */
@RunWith(SpringRunner.class)
@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {"server.address=127.0.0.1", "outgoing-directory=./build"},
        classes = Application.class
)
public class XroadFileServiceTest {

    @LocalServerPort
    int port;

    private Client client;

    @Before
    public void setup() {
        client = new Client("http://127.0.0.1:" + port + "/fileservice",
                "INSTANCE/MEMBER/CLIENT/SUBSYSTEM",
                "INSTANCE/MEMBER/SERVICE/SUBSYSTEM");

    }

    @Test
    public void shouldDownloadTestFile() throws Exception {
        DataHandler result = client.get("/resources/test/test.txt");
        final ByteArrayOutputStream buf = new ByteArrayOutputStream();
        result.writeTo(buf);
        assertTrue(new String(buf.toByteArray(), StandardCharsets.UTF_8).startsWith("TEST"));
    }

    @Test(expected = ErrorResponse.class)
    public void shouldReturnError() throws Exception {
        client.get("/resources/test/notfound.txt");
    }

}

