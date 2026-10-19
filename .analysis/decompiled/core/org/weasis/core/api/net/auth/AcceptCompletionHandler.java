/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.github.scribejava.core.oauth.OAuth20Service
 *  org.slf4j.Logger
 *  org.slf4j.LoggerFactory
 *  org.weasis.core.util.StreamUtil
 *  org.weasis.core.util.StringUtil
 */
package org.weasis.core.api.net.auth;

import com.github.scribejava.core.oauth.OAuth20Service;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.weasis.core.Messages;
import org.weasis.core.api.net.auth.AcceptCallbackHandler;
import org.weasis.core.api.net.auth.AsyncCallbackServerHandler;
import org.weasis.core.util.StreamUtil;
import org.weasis.core.util.StringUtil;

public class AcceptCompletionHandler
implements AcceptCallbackHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(AcceptCompletionHandler.class);
    private static final int BUFFER_SIZE = 1024;
    private static final String CODE_PARAM = "code=";
    private static final String HTTP_OK = "HTTP/1.1 200 OK\r\n";
    private static final String HTTP_NOT_FOUND = "HTTP/1.1 404 Not Found\r\n";
    private final OAuth20Service service;
    private String code;

    public AcceptCompletionHandler(OAuth20Service service) {
        this.service = service;
    }

    @Override
    public void completed(AsynchronousSocketChannel channel, AsyncCallbackServerHandler handler) {
        handler.getSocketChannel().accept(handler, this);
        ByteBuffer buffer = ByteBuffer.allocate(1024);
        channel.read(buffer, buffer, new RequestHandler(this, channel));
    }

    @Override
    public Optional<String> code() {
        return Optional.ofNullable(this.code);
    }

    @Override
    public void code(String code) {
        this.code = code;
    }

    @Override
    public void failed(Throwable t, AsyncCallbackServerHandler handler) {
        LOGGER.error("Socket failed", t);
    }

    @Override
    public OAuth20Service service() {
        return this.service;
    }

    private String extractCodeFromRequest(String request) {
        String string;
        BufferedReader reader = new BufferedReader(new StringReader(request));
        try {
            string = reader.lines().filter(StringUtil::hasText).map(AcceptCompletionHandler::parseCodeFromLine).filter(Objects::nonNull).findFirst().orElse(null);
        }
        catch (Throwable throwable) {
            try {
                try {
                    reader.close();
                }
                catch (Throwable throwable2) {
                    throwable.addSuppressed(throwable2);
                }
                throw throwable;
            }
            catch (IOException e) {
                LOGGER.error("Error parsing request", (Throwable)e);
                return null;
            }
        }
        reader.close();
        return string;
    }

    private static String parseCodeFromLine(String line) {
        int codeIndex = line.indexOf(CODE_PARAM);
        if (codeIndex < 0) {
            return null;
        }
        return line.substring(codeIndex + CODE_PARAM.length()).trim().split("[ &]", 2)[0];
    }

    private static byte[] createResponse(boolean success) {
        String statusLine = success ? HTTP_OK : HTTP_NOT_FOUND;
        Object body = success ? Messages.getString("code.has.been.transmitted") : Messages.getString("code.has.failed") + "\n";
        byte[] bodyBytes = ((String)body).getBytes(StandardCharsets.UTF_8);
        String headers = statusLine + "Content-Type: text/plain; charset=UTF-8\r\nContent-Length: " + bodyBytes.length + "\r\nConnection: close\r\n\r\n";
        byte[] headerBytes = headers.getBytes(StandardCharsets.US_ASCII);
        byte[] response = new byte[headerBytes.length + bodyBytes.length];
        System.arraycopy(headerBytes, 0, response, 0, headerBytes.length);
        System.arraycopy(bodyBytes, 0, response, headerBytes.length, bodyBytes.length);
        return response;
    }

    private final class RequestHandler
    implements CompletionHandler<Integer, ByteBuffer> {
        private final AsynchronousSocketChannel channel;
        final /* synthetic */ AcceptCompletionHandler this$0;

        private RequestHandler(AcceptCompletionHandler acceptCompletionHandler, AsynchronousSocketChannel channel) {
            AcceptCompletionHandler acceptCompletionHandler2 = acceptCompletionHandler;
            Objects.requireNonNull(acceptCompletionHandler2);
            this.this$0 = acceptCompletionHandler2;
            this.channel = channel;
        }

        @Override
        public void completed(Integer result, ByteBuffer buffer) {
            buffer.flip();
            String request = StandardCharsets.UTF_8.decode(buffer).toString();
            String extractedCode = this.this$0.extractCodeFromRequest(request);
            if (extractedCode != null) {
                this.this$0.code(extractedCode);
            }
            this.sendResponse(AcceptCompletionHandler.createResponse(extractedCode != null));
        }

        @Override
        public void failed(Throwable exc, ByteBuffer buffer) {
            LOGGER.error("Cannot get code from callback", exc);
        }

        private void sendResponse(byte[] response) {
            ByteBuffer responseBuffer = ByteBuffer.wrap(response);
            this.channel.write(responseBuffer, this.channel, new WriteHandler(responseBuffer));
        }
    }

    private record WriteHandler(ByteBuffer buffer) implements CompletionHandler<Integer, AsynchronousSocketChannel>
    {
        @Override
        public void completed(Integer result, AsynchronousSocketChannel channel) {
            if (this.buffer.hasRemaining()) {
                channel.write(this.buffer, channel, this);
                return;
            }
            StreamUtil.safeClose((AutoCloseable)channel);
        }

        @Override
        public void failed(Throwable exc, AsynchronousSocketChannel channel) {
            LOGGER.error("Cannot send acknowledge from callback", exc);
            StreamUtil.safeClose((AutoCloseable)channel);
        }
    }
}

