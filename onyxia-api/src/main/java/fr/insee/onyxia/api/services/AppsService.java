package fr.insee.onyxia.api.services;

import fr.insee.onyxia.model.User;
import fr.insee.onyxia.model.service.Service;
import mesosphere.marathon.client.model.v2.Group;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeoutException;

public interface AppsService {

    Group getGroups(String id) throws IOException;
    List<Service> getUserServices(User user) throws InterruptedException, TimeoutException, IOException;
}
