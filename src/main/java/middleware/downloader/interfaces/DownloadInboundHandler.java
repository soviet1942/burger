package middleware.downloader.interfaces;

import io.vertx.core.buffer.Buffer;
import io.vertx.ext.web.client.HttpRequest;

public interface DownloadInboundHandler {

    void handle(HttpRequest<Buffer> httpRequest);

}
