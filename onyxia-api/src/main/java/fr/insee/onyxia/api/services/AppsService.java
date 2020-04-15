package fr.insee.onyxia.api.services;

import fr.insee.onyxia.model.User;
import fr.insee.onyxia.model.service.Service;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeoutException;

public interface AppsService {

    List<Service> getUserServices(User user) throws InterruptedException, TimeoutException, IOException;
}
