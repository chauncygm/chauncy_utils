package cn.chauncy.services;

import com.google.common.util.concurrent.AbstractService;
import com.google.inject.Inject;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import lombok.SneakyThrows;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;

public class HttpFileService extends AbstractService {

    private static final Logger logger = LoggerFactory.getLogger(HttpFileService.class);

    private static final String FILE_ROOT_PATH = "CDN";
    private static final String CONTENT_PATH = "/CDN/";
    private final HttpServer httpServer;

    @Inject
    public HttpFileService() {
        try {
            this.httpServer = HttpServer.create(new InetSocketAddress(11001), 0);
            this.httpServer.createContext(CONTENT_PATH, new HttpFileHandler());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @SneakyThrows
    @Override
    protected void doStart() {
        FileUtils.forceMkdir(new File(FILE_ROOT_PATH));
        httpServer.start();
        logger.info("HttpFileService start success, port: {}", 11001);
    }

    @Override
    protected void doStop() {
        httpServer.stop(3);
    }

    private static class HttpFileHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange httpExchange) throws IOException {
            try {
                String path = httpExchange.getRequestURI().getPath().replace(CONTENT_PATH, "");
                File file = new File(FILE_ROOT_PATH, path).getCanonicalFile();

                if (file.getPath().startsWith(FILE_ROOT_PATH)) {
                    sendError(httpExchange, 403, "Forbidden");
                    return;
                }

                if (!file.exists()) {
                    sendError(httpExchange, 404, "File Not Found");
                    return;
                }
                if (file.isDirectory()) {
                    serveDir(httpExchange, file);
                } else {
                    serveFile(httpExchange, file);
                }

            } catch (Exception e) {
                sendError(httpExchange, 500, "Internal server error" + e.getMessage());
            }
        }

        private void serveFile(HttpExchange exchange, File file) throws IOException {
            byte[] bytes = FileUtils.readFileToByteArray(file);
            exchange.getResponseHeaders().set("Content-Type", "application/octet-stream");
            exchange.sendResponseHeaders(200, bytes.length);
            exchange.getResponseBody().write(bytes);
            exchange.close();

        }

        private void serveDir(HttpExchange exchange, File dir) throws IOException {
            StringBuilder buf = new StringBuilder();
            buf.append("<html><body><h3>Directory List</h3><ul>");
            File[] files = dir.listFiles();
            if (files != null && files.length > 0) {
                for (File file : files) {
                    if (file.isDirectory()) {
                        buf.append("<li><a href=\"").append(file.getName()).append("/\">").append(file.getName()).append("/</a></li>");
                    } else {
                        buf.append("<li><a href=\"").append(file.getName()).append("\">").append(file.getName()).append("</a></li>");
                    }
                }
            } else {
                if (dir.getName().endsWith(FILE_ROOT_PATH)) {
                    buf.append("no files");
                } else {
                    buf.append("<li><a href=\"..\">..</a></li>");
                }
            }
            buf.append("</ul></body></html>");
            byte[] bytes = buf.toString().getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().set("Content-Type", "text/html; charset=utf-8");
            exchange.sendResponseHeaders(200, bytes.length);
            exchange.getResponseBody().write(bytes);
            exchange.close();
        }

        private void sendError(HttpExchange exchange, int code, String message) throws IOException {
            byte[] bytes = message.getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().set("Content-Type", "text/plain; charset=utf-8");
            exchange.sendResponseHeaders(code, bytes.length);
            exchange.getResponseBody().write(message.getBytes());
            exchange.close();
        }
    }
}
