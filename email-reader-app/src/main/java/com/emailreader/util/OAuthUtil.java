package com.emailreader.util;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

public class OAuthUtil {
    private static final Logger logger = LoggerFactory.getLogger(OAuthUtil.class);
    private static final Gson gson = new Gson();

    // OAuth2 configuration (should be in a properties file in production)
    private static final String CLIENT_ID = "your_client_id";
    private static final String CLIENT_SECRET = "your_client_secret";
    private static final String REDIRECT_URI = "http://localhost:8080/email-reader-app/admin/outlook-callback";
    private static final String AUTHORIZE_URL = "https://login.microsoftonline.com/common/oauth2/v2.0/authorize";
    private static final String TOKEN_URL = "https://login.microsoftonline.com/common/oauth2/v2.0/token";
    private static final String GRAPH_API_URL = "https://graph.microsoft.com/v1.0";
    private static final String SCOPE = "offline_access https://graph.microsoft.com/mail.read";

    public static String buildAuthorizationUrl(String state) {
        try {
            String url = AUTHORIZE_URL +
                    "?client_id=" + URLEncoder.encode(CLIENT_ID, StandardCharsets.UTF_8) +
                    "&response_type=code" +
                    "&redirect_uri=" + URLEncoder.encode(REDIRECT_URI, StandardCharsets.UTF_8) +
                    "&scope=" + URLEncoder.encode(SCOPE, StandardCharsets.UTF_8) +
                    "&response_mode=query" +
                    "&state=" + URLEncoder.encode(state, StandardCharsets.UTF_8);

            logger.info("Generated authorization URL for state: {}", state);
            return url;
        } catch (Exception e) {
            logger.error("Error building authorization URL", e);
            throw new RuntimeException("Error building authorization URL", e);
        }
    }

    public static Map<String, Object> exchangeCodeForToken(String code) {
        try (CloseableHttpClient client = HttpClients.createDefault()) {
            HttpPost post = new HttpPost(TOKEN_URL);
            
            // Prepare the token request
            String params = "client_id=" + CLIENT_ID +
                    "&client_secret=" + CLIENT_SECRET +
                    "&code=" + code +
                    "&redirect_uri=" + URLEncoder.encode(REDIRECT_URI, StandardCharsets.UTF_8) +
                    "&grant_type=authorization_code";

            StringEntity entity = new StringEntity(params);
            post.setEntity(entity);
            post.setHeader("Content-Type", "application/x-www-form-urlencoded");

            try (CloseableHttpResponse response = client.execute(post)) {
                String jsonResponse = EntityUtils.toString(response.getEntity());
                JsonObject jsonObject = gson.fromJson(jsonResponse, JsonObject.class);

                if (response.getStatusLine().getStatusCode() != 200) {
                    logger.error("Error exchanging code for token. Response: {}", jsonResponse);
                    throw new RuntimeException("Error exchanging code for token");
                }

                Map<String, Object> tokenInfo = new HashMap<>();
                tokenInfo.put("access_token", jsonObject.get("access_token").getAsString());
                tokenInfo.put("refresh_token", jsonObject.get("refresh_token").getAsString());
                tokenInfo.put("expires_in", jsonObject.get("expires_in").getAsInt());
                
                // Calculate token expiration time
                int expiresIn = jsonObject.get("expires_in").getAsInt();
                LocalDateTime expiresAt = LocalDateTime.now().plusSeconds(expiresIn);
                tokenInfo.put("expires_at", expiresAt);

                return tokenInfo;
            }
        } catch (Exception e) {
            logger.error("Error exchanging code for token", e);
            throw new RuntimeException("Error exchanging code for token", e);
        }
    }

    public static Map<String, String> getUserInfo(String accessToken) {
        try (CloseableHttpClient client = HttpClients.createDefault()) {
            HttpGet get = new HttpGet(GRAPH_API_URL + "/me");
            get.setHeader("Authorization", "Bearer " + accessToken);

            try (CloseableHttpResponse response = client.execute(get)) {
                HttpEntity entity = response.getEntity();
                String jsonResponse = EntityUtils.toString(entity);
                JsonObject jsonObject = gson.fromJson(jsonResponse, JsonObject.class);

                if (response.getStatusLine().getStatusCode() != 200) {
                    logger.error("Error getting user info. Response: {}", jsonResponse);
                    throw new RuntimeException("Error getting user info");
                }

                Map<String, String> userInfo = new HashMap<>();
                userInfo.put("email", jsonObject.get("userPrincipalName").getAsString());
                userInfo.put("displayName", jsonObject.get("displayName").getAsString());

                return userInfo;
            }
        } catch (IOException e) {
            logger.error("Error getting user info", e);
            throw new RuntimeException("Error getting user info", e);
        }
    }

    public static Map<String, Object> refreshToken(String refreshToken) {
        try (CloseableHttpClient client = HttpClients.createDefault()) {
            HttpPost post = new HttpPost(TOKEN_URL);
            
            String params = "client_id=" + CLIENT_ID +
                    "&client_secret=" + CLIENT_SECRET +
                    "&refresh_token=" + refreshToken +
                    "&grant_type=refresh_token";

            StringEntity entity = new StringEntity(params);
            post.setEntity(entity);
            post.setHeader("Content-Type", "application/x-www-form-urlencoded");

            try (CloseableHttpResponse response = client.execute(post)) {
                String jsonResponse = EntityUtils.toString(response.getEntity());
                JsonObject jsonObject = gson.fromJson(jsonResponse, JsonObject.class);

                if (response.getStatusLine().getStatusCode() != 200) {
                    logger.error("Error refreshing token. Response: {}", jsonResponse);
                    throw new RuntimeException("Error refreshing token");
                }

                Map<String, Object> tokenInfo = new HashMap<>();
                tokenInfo.put("access_token", jsonObject.get("access_token").getAsString());
                tokenInfo.put("refresh_token", jsonObject.get("refresh_token").getAsString());
                tokenInfo.put("expires_in", jsonObject.get("expires_in").getAsInt());

                int expiresIn = jsonObject.get("expires_in").getAsInt();
                LocalDateTime expiresAt = LocalDateTime.now().plusSeconds(expiresIn);
                tokenInfo.put("expires_at", expiresAt);

                return tokenInfo;
            }
        } catch (Exception e) {
            logger.error("Error refreshing token", e);
            throw new RuntimeException("Error refreshing token", e);
        }
    }
}
