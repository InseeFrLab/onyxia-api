package fr.insee.onyxia.api.services.impl;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import fr.insee.onyxia.api.services.AppsService;
import mesosphere.marathon.client.Marathon;
import mesosphere.marathon.client.MarathonClient;
import mesosphere.marathon.client.model.v2.GetAppResponse;
import mesosphere.marathon.client.model.v2.GetAppsResponse;
import mesosphere.marathon.client.model.v2.VersionedApp;

@Service
public class MarathonAppsService implements AppsService {


   @Autowired(required = false)
   Marathon marathon;
   
   @Override
   public List<VersionedApp> getServices(Map<String, String> params) {
      GetAppsResponse appsResponse = marathon.getApps(params);
      return appsResponse.getApps();
   }

   @Override
   public VersionedApp getServiceById(String id) {
      GetAppResponse appsResponse = marathon.getApp(id);
      VersionedApp app = appsResponse.getApp();
      return app;
   }

}
