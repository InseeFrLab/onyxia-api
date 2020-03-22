package fr.insee.onyxia.api.services;

import fr.insee.onyxia.model.User;

public interface UserDataService {
   public void saveUserData(User user);
   
   public void fetchUserData(User user);
}
