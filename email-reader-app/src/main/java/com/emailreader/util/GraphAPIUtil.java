package com.emailreader.util;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GraphAPIUtil {
    private static final Logger logger = LoggerFactory.getLogger(GraphAPIUtil.class);
    private static final Gson gson = new Gson();
    private static final String GRAPH_API_URL = "https://graph.microsoft.com/v1.0";

    public static List<Map<String, String>> fetchEmails(String accessToken, int pageSize, String nextPageToken) {
        try (CloseableHttpClient client = HttpClients.createDefault()) {
            StringBuilder urlBuilder = new StringBuilder(GRAPH_API_URL + "/me/messages");
            urlBuilder.append("?$select=subject,from,receivedDateTime,bodyPreview");
            urlBuilder.append("&$top=" + pageSize);
            urlBuilder.append("&$orderby=receivedDateTime desc");
            
            if (nextPageToken != null && !nextPageToken.isEmpty()) {
                urlBuilder.append("&$skiptoken=" + nextPageToken);
            }

            HttpGet get = new HttpGet(urlBuilder.toString());
            get.setHeader("Authorization", "Bearer " + accessToken);
            get.setHeader("Accept", "application/json");

            try (CloseableHttpResponse response = client.execute(get)) {
                String jsonResponse = EntityUtils.toString(response.getEntity());
                
                if (response.getStatusLine().getStatusCode() != 200) {
                    logger.error("Error fetching emails. Response: {}", jsonResponse);
                    throw new RuntimeException("Error fetching emails");
                }

                JsonObject jsonObject = gson.fromJson(jsonResponse, JsonObject.class);
                JsonArray messages = jsonObject.getAsJsonArray("value");
                List<Map<String, String>> emails = new ArrayList<>();

                for (int i = 0; i < messages.size(); i++) {
                    JsonObject message = messages.get(i).getAsJsonObject();
                    Map<String, String> email = new HashMap<>();
                    
                    email.put("id", message.get("id").getAsString());
                    email.put("subject", message.get("subject").getAsString());
                    
                    JsonObject from = message.getAsJsonObject("from")
                                          .getAsJsonObject("emailAddress");
                    email.put("from", from.get("name").getAsString() + 
                                    " <" + from.get("address").getAsString() + ">");
                    
                    // Format the date
                    String receivedDateTime = message.get("receivedDateTime").getAsString();
                    ZonedDateTime dateTime = ZonedDateTime.parse(receivedDateTime);
                    String formattedDate = dateTime.format(
                        DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm")
                    );
                    email.put("receivedDateTime", formattedDate);
                    
                    email.put("bodyPreview", message.get("bodyPreview").getAsString());
                    
                    emails.add(email);
                }

                return emails;
            }
        } catch (Exception e) {
            logger.error("Error fetching emails", e);
            throw new RuntimeException("Error fetching emails", e);
        }
    }

    public static Map<String, String> fetchEmailContent(String accessToken, String emailId) {
        try (CloseableHttpClient client = HttpClients.createDefault()) {
            String url = GRAPH_API_URL + "/me/messages/" + emailId;
            url += "?$select=subject,body,from,receivedDateTime,toRecipients,ccRecipients";

            HttpGet get = new HttpGet(url);
            get.setHeader("Authorization", "Bearer " + accessToken);
            get.setHeader("Accept", "application/json");

            try (CloseableHttpResponse response = client.execute(get)) {
                String jsonResponse = EntityUtils.toString(response.getEntity());
                
                if (response.getStatusLine().getStatusCode() != 200) {
                    logger.error("Error fetching email content. Response: {}", jsonResponse);
                    throw new RuntimeException("Error fetching email content");
                }

                JsonObject message = gson.fromJson(jsonResponse, JsonObject.class);
                Map<String, String> emailContent = new HashMap<>();

                emailContent.put("subject", message.get("subject").getAsString());
                
                JsonObject from = message.getAsJsonObject("from")
                                      .getAsJsonObject("emailAddress");
                emailContent.put("from", from.get("name").getAsString() + 
                                      " <" + from.get("address").getAsString() + ">");

                // Format the date
                String receivedDateTime = message.get("receivedDateTime").getAsString();
                ZonedDateTime dateTime = ZonedDateTime.parse(receivedDateTime);
                String formattedDate = dateTime.format(
                    DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm")
                );
                emailContent.put("receivedDateTime", formattedDate);

                // Get body content
                JsonObject body = message.getAsJsonObject("body");
                emailContent.put("contentType", body.get("contentType").getAsString());
                emailContent.put("content", body.get("content").getAsString());

                // Process recipients
                StringBuilder toRecipients = new StringBuilder();
                JsonArray to = message.getAsJsonArray("toRecipients");
                for (int i = 0; i < to.size(); i++) {
                    JsonObject recipient = to.get(i).getAsJsonObject()
                                           .getAsJsonObject("emailAddress");
                    if (i > 0) toRecipients.append(", ");
                    toRecipients.append(recipient.get("name").getAsString())
                               .append(" <")
                               .append(recipient.get("address").getAsString())
                               .append(">");
                }
                emailContent.put("to", toRecipients.toString());

                // Process CC recipients if present
                if (message.has("ccRecipients")) {
                    StringBuilder ccRecipients = new StringBuilder();
                    JsonArray cc = message.getAsJsonArray("ccRecipients");
                    for (int i = 0; i < cc.size(); i++) {
                        JsonObject recipient = cc.get(i).getAsJsonObject()
                                               .getAsJsonObject("emailAddress");
                        if (i > 0) ccRecipients.append(", ");
                        ccRecipients.append(recipient.get("name").getAsString())
                                   .append(" <")
                                   .append(recipient.get("address").getAsString())
                                   .append(">");
                    }
                    emailContent.put("cc", ccRecipients.toString());
                }

                return emailContent;
            }
        } catch (Exception e) {
            logger.error("Error fetching email content", e);
            throw new RuntimeException("Error fetching email content", e);
        }
    }
}
