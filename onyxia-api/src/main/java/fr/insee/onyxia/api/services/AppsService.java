package fr.insee.onyxia.api.services;

import java.util.List;
import java.util.Map;

import mesosphere.marathon.client.model.v2.VersionedApp;

public interface AppsService {
   
   VersionedApp getServiceById(String id);
}
