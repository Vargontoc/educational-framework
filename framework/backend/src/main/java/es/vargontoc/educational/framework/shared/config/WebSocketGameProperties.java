package es.vargontoc.educational.framework.shared.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "app.ws.game")
public class WebSocketGameProperties {

    private int sendTimeLimitMs = 10000;
    private int bufferSizeLimit = 524288;
    private long maxSessionIdleTimeoutMs = 120000;
    private int maxTextMessageBufferSize = 65536;
    private int maxBinaryMessageBufferSize = 524288;

    public int getSendTimeLimitMs() {
        return sendTimeLimitMs;
    }

    public void setSendTimeLimitMs(int sendTimeLimitMs) {
        this.sendTimeLimitMs = sendTimeLimitMs;
    }

    public int getBufferSizeLimit() {
        return bufferSizeLimit;
    }

    public void setBufferSizeLimit(int bufferSizeLimit) {
        this.bufferSizeLimit = bufferSizeLimit;
    }

    public long getMaxSessionIdleTimeoutMs() {
        return maxSessionIdleTimeoutMs;
    }

    public void setMaxSessionIdleTimeoutMs(long maxSessionIdleTimeoutMs) {
        this.maxSessionIdleTimeoutMs = maxSessionIdleTimeoutMs;
    }

    public int getMaxTextMessageBufferSize() {
        return maxTextMessageBufferSize;
    }

    public void setMaxTextMessageBufferSize(int maxTextMessageBufferSize) {
        this.maxTextMessageBufferSize = maxTextMessageBufferSize;
    }

    public int getMaxBinaryMessageBufferSize() {
        return maxBinaryMessageBufferSize;
    }

    public void setMaxBinaryMessageBufferSize(int maxBinaryMessageBufferSize) {
        this.maxBinaryMessageBufferSize = maxBinaryMessageBufferSize;
    }
}
