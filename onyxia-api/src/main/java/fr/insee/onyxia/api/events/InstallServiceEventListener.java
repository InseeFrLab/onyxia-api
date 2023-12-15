package fr.insee.onyxia.api.events;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class InstallServiceEventListener {
    private final Logger logger = LoggerFactory.getLogger(InstallServiceEventListener.class);

    @EventListener
    public void onInstallServiceEvent(InstallServiceEvent event) throws IOException {
        Map<String, Object> myMap = new HashMap<>();

        if (StringUtils.isNotEmpty(event.getClass().getName())) {
            myMap.put("name", event.createServiceDTO.getName());
        }

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.writeValue(new File("target/event.json"), myMap);
        logger.info(
                "[EVENT] name "
                        + event.createServiceDTO.getName()
                        + " du catalog "
                        + event.createServiceDTO.getCatalogId());
    }

    @EventListener
    public void onUninstallServiceEvent(UninstallServiceEvent event) {
        logger.info("[Event] uninstall service " + event.getName());
    }
}
