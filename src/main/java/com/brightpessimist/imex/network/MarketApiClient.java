package com.brightpessimist.imex.network;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;

import com.brightpessimist.imex.config.Config;
import com.brightpessimist.imex.config.TradingConfig;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class MarketApiClient {

    private static final Gson GSON = new Gson();
    private static MarketData cachedData = null;
    private static long lastFetchTime = 0;
    private static final long CACHE_DURATION_MS = 60000; // Cache for 60 seconds

    public interface MarketCallback {

        void onSuccess(MarketData data);

        void onError(String errorMessage);
    }

    public static MarketData performHttpRequest() throws Exception {
        URL url = URI.create(Config.api_url)
            .toURL();
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();

        try {
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Accept", "application/json");
            conn.setConnectTimeout(5000);
            conn.setReadTimeout(5000);

            int status = conn.getResponseCode();
            InputStream stream = (status >= 200 && status < 300) ? conn.getInputStream() : conn.getErrorStream();

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))) {
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }

                if (status >= 200 && status < 300) {
                    JsonObject jsonObject = new JsonParser().parse(response.toString())
                        .getAsJsonObject();
                    if (jsonObject.get("errors")
                        .getAsBoolean()) {
                        throw new Exception("Erros in api response!");
                    }
                    jsonObject.get("data")
                        .getAsJsonObject()
                        .get("goods");
                    return GSON.fromJson(
                        jsonObject.get("data")
                            .getAsJsonObject()
                            .get("goods"),
                        MarketData.class);
                } else {
                    throw new Exception("HTTP Error " + status + ": " + response.toString());
                }
            }
        } catch (Exception e) {
            return GSON.fromJson(TradingConfig.get_goods_json(), MarketData.class);
        } finally {
            conn.disconnect();
        }
    }

    /**
     * Returns the last known good data, even if offline.
     */
    public static MarketData getCachedData() {
        return cachedData;
    }
}
