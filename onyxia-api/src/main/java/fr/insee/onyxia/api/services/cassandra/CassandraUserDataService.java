package fr.insee.onyxia.api.services.cassandra;

import com.datastax.driver.core.Session;
import fr.insee.onyxia.api.services.UserDataService;
import fr.insee.onyxia.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

@Service
@ConditionalOnProperty(name = "userdata.source", havingValue = "cassandra")
public class CassandraUserDataService implements UserDataService {
   
   @Autowired
   private CassandraConnector connector;

   @Override
   public void saveUserData(User user) {
      Session session = connector.getSession();

      KeyspaceRepository sr = new KeyspaceRepository(session);
      sr.useKeyspace("library");

      UserRepository ur = new UserRepository(session);
      
      ur.updateUser(user);
   }

   @Override
   public void fetchUserData(User user) {
      Session session = connector.getSession();

      KeyspaceRepository sr = new KeyspaceRepository(session);
      sr.useKeyspace("library");

      UserRepository ur = new UserRepository(session);

      ur.upgradeUser(user);
   }

}
