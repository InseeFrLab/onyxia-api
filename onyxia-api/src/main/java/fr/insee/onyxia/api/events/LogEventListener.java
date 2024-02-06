package fr.insee.onyxia.api.events;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Component;

@EnableAsync
@Component
@ConditionalOnProperty(name = "event.logging.enabled", havingValue = "true")
public class LogEventListener {

    private final ObjectMapper objectMapper;

    private static final Logger LOGGER = LoggerFactory.getLogger("onyxia.sh.events");

    @Autowired
    public LogEventListener(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Async
    @EventListener
    public void onOnyxiaEvent(OnyxiaEvent onyxiaEvent) throws JsonProcessingException {

        LOGGER.info(objectMapper.writeValueAsString(onyxiaEvent));
    }
}
