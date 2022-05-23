package test.Encodability;

import com.google.gson.Gson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.*;

import org.apache.http.HttpClientConnection;

import java.net.http.HttpClient;    //issues if not with Java1.7
import java.net.http.HttpRequest;
import java.net.http.HttpResponse; 
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class GraphDBInterface {

    Logger LOGGER = LoggerFactory.getLogger(this.getClass().getName());

    private String graphUrl = "https://graphdb.interactions.ics.unisg.ch/repositories/";
    private final String graphQueryURL;
    private final String graphUpdateURL;

    Authenticator ourAuthenticator;

    
    public GraphDBInterface(final String graphName, final String userName, final String password) {
        this.graphUrl = this.graphUrl + graphName;
        this.graphQueryURL = this.graphUrl + "?query=";
        this.graphUpdateURL = this.graphUrl + "/statements";

        final PasswordAuthentication ourAuth = new PasswordAuthentication (userName, password.toCharArray());
        this.ourAuthenticator = new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return ourAuth;
            }
        };
    }

    public Map query(String queryString) throws Exception {
        
        HttpClient client = HttpClient.newHttpClient();

        HttpRequest request = null;
        try {
            String queryUrlEncoded = URLEncoder.encode(queryString, StandardCharsets.UTF_8);

            request = HttpRequest.newBuilder()
                    .uri(new URI(graphQueryURL+queryUrlEncoded))
                    .header("Accept", "application/sparql-results+json")
                    .GET()
                    .build();

            HttpResponse<String> response = HttpClient.newBuilder()
                    .authenticator(ourAuthenticator).build()
                    .send(request, HttpResponse.BodyHandlers.ofString());
            LOGGER.debug("Response Status Code: " + response.statusCode());

            Map jsonJavaRootObject = new Gson().fromJson(response.body(), Map.class);
            return jsonJavaRootObject;
        } catch (URISyntaxException | InterruptedException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return new HashMap();
    }
    
    public int update(String insertQueryString) throws Exception {

        HttpClient client = HttpClient.newHttpClient();
        String queryUrlEncoded = URLEncoder.encode(insertQueryString, StandardCharsets.UTF_8);

        HttpRequest request = null;
        try {
            request = HttpRequest.newBuilder()
                    .uri(new URI(graphUpdateURL))
                    .POST(HttpRequest.BodyPublishers.ofString("update=" + queryUrlEncoded))
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .build();

            HttpResponse<String> response = HttpClient.newBuilder()
                    .authenticator(ourAuthenticator).build()
                    .send(request, HttpResponse.BodyHandlers.ofString());
            LOGGER.debug("Response Status Code: " + response.statusCode());

            return response.statusCode();
        } catch (URISyntaxException | InterruptedException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return 0;
    }

}   
