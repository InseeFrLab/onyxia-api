package io.github.inseefrlab.helmwrapper.events;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;


@Component
public class InstallAppEventListener {
   private final Logger logger = LoggerFactory.getLogger(InstallAppEventListener.class);
   @EventListener
   public void onInstallAppEvent(InstallAppEvent event){
      logger.info("[EVENT] " + event.helmInstaller.getName() + " is installed");
   }

}
