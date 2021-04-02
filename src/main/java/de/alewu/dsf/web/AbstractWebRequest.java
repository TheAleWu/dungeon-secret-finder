package de.alewu.dsf.web;

import de.alewu.dsf.exceptions.DungeonSecretFinderException;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

public abstract class AbstractWebRequest {

    private final String url;

    public AbstractWebRequest(String url) {
        this.url = Objects.requireNonNull(url);
    }

    public String getUrl() {
        return url;
    }

    public CompletableFuture<String> send() {
        return CompletableFuture.supplyAsync(() -> {
            StringBuilder sb = new StringBuilder();
            try (CloseableHttpClient httpclient = HttpClients.createDefault()) {
                HttpGet httpGet = new HttpGet(url);
                try (CloseableHttpResponse response1 = httpclient.execute(httpGet)) {
                    HttpEntity entity = response1.getEntity();

                    try (BufferedReader reader = new BufferedReader(new InputStreamReader(entity.getContent()))) {
                        String line;
                        while ((line = reader.readLine()) != null) {
                            sb.append(line).append("\n");
                        }
                    }

                    EntityUtils.consume(entity);
                }
            } catch (IOException e) {
                throw new DungeonSecretFinderException("Error while requesting web resource at " + url, e);
            }
            return sb.length() == 0 ? sb.toString() : sb.substring(0, sb.length() - 1);
        });
    }
}
