package fr.insee.onyxia.api.controller.pub;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import fr.insee.onyxia.api.configuration.metrics.CustomMetrics;
import fr.insee.onyxia.api.services.AppsService;
import io.swagger.v3.oas.annotations.tags.Tag;
import mesosphere.marathon.client.model.v2.VersionedApp;

@Tag(name = "Our Lab",description = "Shared services")
@RequestMapping("/public/our-lab")
@RestController
public class OurLabController {
   
   @Autowired
   private AppsService appsService;
   
   

   //TODO : Use orchestrator-agnostic objects instead of marathon-related VersionedApp
   @GetMapping("/apps")
   public List<VersionedApp> getServices() {
       Map<String, String> params = new HashMap<>();
       params.put("label", "ONYXIA_ACCUEIL==true");

       return appsService.getServices(params);
   }

   @GetMapping("/apps/{id}")
   public VersionedApp getSingleService(@PathVariable String id) {
       return appsService.getServiceById(id);
   }
}
