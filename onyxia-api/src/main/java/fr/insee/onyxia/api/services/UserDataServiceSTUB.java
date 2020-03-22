package fr.insee.onyxia.api.services;


import fr.insee.onyxia.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnSingleCandidate;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;

@Service
@ConditionalOnSingleCandidate(UserDataService.class)
public class UserDataServiceSTUB implements UserDataService {

    private final Logger logger = LoggerFactory.getLogger(UserDataServiceSTUB.class);

    @PostConstruct
    public void init() {
        logger.warn("Warning : using STUB userdata source");
    }

    @Override
    public void saveUserData(User user) {
        // TODO Auto-generated method stub

    }

    @Override
    public void fetchUserData(User user) {
        // TODO Auto-generated method stub

    }

}