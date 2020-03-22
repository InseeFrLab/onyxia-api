package fr.insee.onyxia.api.services.control;

import fr.insee.onyxia.model.User;
import fr.insee.onyxia.model.catalog.UniversePackage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
@PropertySource("classpath:userEnforcer.properties")
public class UserEnforcer implements AdmissionController {

    @Value("${data.enforcer.dns}")
    private String dns;

    @Value("${data.enforcer.git}")
    private String git;

    @Value("${s3.url.alpha}")
    private String keyLocation;

    @Override
    public boolean validateContract(User user, UniversePackage pkg, Object data) {
        return enforceUser(user,pkg,data);
    }

    public boolean enforceUser(User user, UniversePackage pkg, Object object) {
        UUID uuid = UUID.randomUUID();
        String instanceID = Long.toString(-uuid.getLeastSignificantBits());
        Map<String,String> userValues = new HashMap<String,String>();
        userValues.put("\\[\\$IDEP\\]",user.getIdep());
        userValues.put("\\[\\$USERMAIL\\]",user.getEmail());
        userValues.put("\\[\\$USERNAME\\]",user.getNomComplet());
        userValues.put("\\[\\$USERPASSWORD\\]",user.getPassword());
        //	userValues.put("\\[\\$USERKEY\\]",user.getSshKeyUrl());
        userValues.put("\\[\\$USERKEY\\]",keyLocation+user.getIdep()+"_rsa.gpg");
        userValues.put("\\[\\$STATUS\\]", pkg.getStatus());
        userValues.put("\\[\\$GIT\\]",git);
        userValues.put("\\[\\$DNS\\]",dns);
        userValues.put("\\[\\$UUID\\]",instanceID);
        return pkg.getProperties().enforceUser(userValues,object);
    }
}
