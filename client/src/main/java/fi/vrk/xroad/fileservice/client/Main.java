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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

/**
 * Command line interface for the XRoad FileService Client
 */
public final class Main {
    private static final Logger LOG = Logger.getLogger(Client.class.getName());

    private Main() {
    }

    /**
     * main entry point
     *
     * @param args url clientId serviceId filename
     */
    public static void main(String[] args) {
        int status = 1;

        if (args.length == 0) {
            usage("");
            System.exit(2);
        }

        try {
            Client client = new Client(required(args, 0), required(args, 1), required(args, 2));
            switch (required(args, 3).toLowerCase()) {
                case "get":
                    handleGet(client, required(args, 4), optional(args, 5));
                    break;
                case "list":
                    handleList(client);
                    break;
                case "put":
                    handlePut(client, required(args, 4), optional(args, 5));
                    break;
                default:
                    throw new IllegalArgumentException("Unknown command: " + args[3]);
            }
            status = 0;
        } catch (IllegalArgumentException e) {
            usage(e.getMessage());
        } catch (ErrorResponse e) {
            LOG.severe(e.getMessage());
        } catch (UncheckedIOException e) {
            if (e.getCause() instanceof FileAlreadyExistsException) {
                LOG.severe("The output file already exists.");
            } else {
                LOG.severe(e.getMessage());
            }
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Unexpected error", e);
            status = 127;
        }

        System.exit(status);
    }

    private static void handleList(Client client) throws ErrorResponse {
        client.list().forEach(System.out::println);
    }

    private static void handleGet(Client client, String fileName, Optional<String> outputName)
            throws ErrorResponse, IOException {

        try (OutputStream out = outputName
                .map(v -> ".".equals(v) ? Paths.get(fileName).getFileName() : Paths.get(v))
                .map(Throwing.wrap(p -> Files.newOutputStream(p, StandardOpenOption.CREATE_NEW)))
                .orElse(System.out)) {
            client.get(fileName).writeTo(out);
            out.flush();
        }
    }

    private static void handlePut(Client client, String localName, Optional<String> remoteName)
            throws IOException, ErrorResponse {
        if ("-".equals(localName) && !remoteName.isPresent()) {
            throw new IllegalArgumentException("Must specify remote file name if reading from standard input");
        }
        try (InputStream in = "-".equals(localName) ? System.in
                : Files.newInputStream(Paths.get(localName), StandardOpenOption.READ)) {
            client.put(remoteName.orElse(Paths.get(localName).getFileName().toString()), in);
        }
    }

    private static String required(String[] args, int index) {
        if (index < args.length) {
            return args[index];
        }
        throw new IllegalArgumentException("Required argument missing");
    }

    private static Optional<String> optional(String[] args, int index) {
        return index < args.length ? Optional.of(args[index]) : Optional.empty();
    }

    private static void usage(String message) {
        if (!message.isEmpty()) System.err.println(message);

        System.err.print("Usage: (java -jar ...) <url> <clientId> <memberId> <command> [command arguments]\n"
                + "\turl     : service or client security server URL\n"
                + "\tclientId: instanceId/memberClass/memberCode/subsystemCode\n"
                + "\tmemberId: service memberId, same format as clientId\n"
                + "\tfilename: name of the file to fetch\n"
                + "\tcommand : get | put | list\n"
                + "\n"
                + "\t          get <remote filename> [local filename]\n"
                + "\t          remote filename : name of the remote file to fetch\n"
                + "\t          local filename  : name of the output file, or standard output if omitted\n"
                + "\n"
                + "\t          put <local filename> [remote file name]\n"
                + "\t          local filename  : name of the input file, or '-' for standard input\n"
                + "\t          remote filename : name of the remote file (same as local file if omitted)\n"
                + "\n"
                + "\t          list\n"
                + "\t          (lists downloadable files)"
                + "\n");
    }

    //set up java logging configuration from classpath
    static {
        if (System.getProperty("java.util.logging.config.file") == null) {
            try (InputStream is = Client.class.getResourceAsStream("/logging.properties")) {
                LogManager.getLogManager().readConfiguration(is);
            } catch (IOException e) {
                //ignore
            }
        }
    }
}
