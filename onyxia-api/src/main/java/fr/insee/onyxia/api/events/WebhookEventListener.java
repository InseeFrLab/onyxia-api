package fr.insee.onyxia.api.events;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Component;

@EnableAsync
@Component
@ConditionalOnProperty(name = "event.webhook.enabled", havingValue = "true")
public class WebhookEventListener {

    private final ObjectMapper objectMapper;

    private static final MediaType JSON = MediaType.get("application/json");

    private static final Logger LOGGER = LoggerFactory.getLogger(WebhookEventListener.class);

    @Value("${event.webhook.url}")
    private String url;

    @Value("${event.webhook.includes}")
    private List<String> includes = new ArrayList<>();

    @Value("${event.webhook.excludes}")
    private List<String> excludes = new ArrayList<>();

    private final OkHttpClient httpClient;

    @Autowired
    public WebhookEventListener(ObjectMapper objectMapper, OkHttpClient okHttpClient) {
        this.objectMapper = objectMapper;
        this.httpClient = okHttpClient;
    }

    @Async
    @EventListener
    public void onOnyxiaEvent(OnyxiaEvent onyxiaEvent) throws JsonProcessingException {
        if (!includes.isEmpty()) {
            if (!includes.contains(onyxiaEvent.getType())) {
                return;
            }
        }
        if (!excludes.isEmpty()) {
            if (excludes.contains(onyxiaEvent.getType())) {
                return;
            }
        }
        RequestBody body = RequestBody.create(objectMapper.writeValueAsString(onyxiaEvent), JSON);
        Request request = new Request.Builder().url(url).post(body).build();
        try {
            httpClient.newCall(request).execute();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public ObjectMapper getObjectMapper() {
        return objectMapper;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public List<String> getIncludes() {
        return includes;
    }

    public void setIncludes(List<String> includes) {
        this.includes = includes;
    }

    public List<String> getExcludes() {
        return excludes;
    }

    public void setExcludes(List<String> excludes) {
        this.excludes = excludes;
    }

    public OkHttpClient getHttpClient() {
        return httpClient;
    }
}
