package com.amazonaws.kinesisvideo.signaling.okhttp;

import static org.awaitility.Awaitility.await;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;
import android.util.Log;
import androidx.annotation.NonNull;

import com.amazonaws.kinesisvideo.signaling.SignalingListener;
import com.amazonaws.kinesisvideo.utils.Constants;

import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

/**
 * An OkHttp based WebSocket client.
 */
class WebSocketClient {

    private static final String TAG = "WebSocketClient";
    private final WebSocket webSocket;
    private volatile boolean isOpen = false;

    WebSocketClient(@NonNull final String uri, @NonNull final SignalingListener signalingListener) {

        // OkHttpClient client = new OkHttpClient.Builder().build();

        // 1. 创建信任所有证书的TrustManager（与您提供的代码一致）
        TrustManager[] trustAllCerts = new TrustManager[]{
            new X509TrustManager() {
                public void checkClientTrusted(X509Certificate[] chain, String authType) {}
                public void checkServerTrusted(X509Certificate[] chain, String authType) {}
                public X509Certificate[] getAcceptedIssuers() { return new X509Certificate[0]; }
            }
        };
        
        // 2. 配置SSLContext（与您提供的代码一致）
        SSLContext sslContext = null;
        try {
            sslContext = SSLContext.getInstance("SSL");
            sslContext.init(null, trustAllCerts, new SecureRandom());
        } catch (Exception e) {
            Log.e(TAG, "SSL context initialization failed", e);
        }

        // 3. 创建OkHttpClient并配置SSL
        OkHttpClient client = new OkHttpClient.Builder()
            .sslSocketFactory(sslContext.getSocketFactory(), (X509TrustManager)trustAllCerts[0])
            .hostnameVerifier((hostname, session) -> true) // 信任所有主机名
            .build();

        // 4. 创建WebSocket请求
        String userAgent = (Constants.APP_NAME + "/" + Constants.VERSION + " " + System.getProperty("http.agent")).trim();

        Log.d(TAG, "User agent: " + userAgent);

        Request request = new Request.Builder()
                .url(uri)
                .addHeader("User-Agent", userAgent)
                .addHeader("Host", "ClientId=ConsumerViewer_12345")
                .build();

        webSocket = client.newWebSocket(request, new WebSocketListener() {
            @Override
            public void onOpen(@NonNull WebSocket webSocket, @NonNull Response response) {
                Log.d(TAG, "WebSocket connection opened");
                isOpen = true;
            }

            @Override
            public void onMessage(@NonNull WebSocket webSocket, @NonNull String message) {
                Log.d(TAG, "Websocket received a message: " + message);
                signalingListener.getWebsocketListener().onMessage(webSocket, message);
            }

            @Override
            public void onClosed(@NonNull WebSocket webSocket, int code, @NonNull String reason) {
                Log.d(TAG, "WebSocket connection closed: " + reason);
                isOpen = false;
            }

            @Override
            public void onFailure(@NonNull WebSocket webSocket, @NonNull Throwable t, Response response) {
                Log.e(TAG, "WebSocket connection failed", t);
                isOpen = false;
                signalingListener.onException((Exception) t);
            }
        });

        // Await WebSocket connection
        await().atMost(10, TimeUnit.SECONDS).until(WebSocketClient.this::isOpen);
    }

    void send(String message) {
        if (isOpen) {
            if (webSocket.send(message)) {
                Log.d(TAG, "Successfully sent " + message);
            } else {
                Log.d(TAG, "Could not send " + message + " as the connection may have closing, closed, or canceled.");
            }
        } else {
            Log.d(TAG, "Cannot send the websocket message as it is not open.");
        }
    }

    void disconnect() {
        if (isOpen) {
            if (webSocket.close(1000, "Disconnect requested")) {
                Log.d(TAG, "Websocket successfully disconnected.");
            } else {
                Log.d(TAG, "Websocket could not disconnect in a graceful shutdown. Going to cancel it to release resources.");
                webSocket.cancel();
            }
        } else {
            Log.d(TAG, "Cannot close the websocket as it is not open.");
        }
    }

    boolean isOpen() {
        return isOpen;
    }
}
