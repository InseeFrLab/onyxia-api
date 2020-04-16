package fr.insee.onyxia.api.services;

import fr.insee.onyxia.model.User;
import fr.insee.onyxia.model.service.Service;
import org.springframework.scheduling.annotation.Async;

import java.io.IOException;
import java.text.ParseException;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeoutException;

public interface AppsService {

    @Async
    CompletableFuture<List<Service>> getUserServices(User user) throws InterruptedException, TimeoutException, IOException, ParseException;
}
