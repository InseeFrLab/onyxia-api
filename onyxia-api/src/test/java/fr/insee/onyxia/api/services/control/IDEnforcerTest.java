package fr.insee.onyxia.api.services.control;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import fr.insee.onyxia.api.services.control.marathon.IDEnforcer;
import fr.insee.onyxia.api.services.control.utils.PublishContext;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;

import fr.insee.onyxia.model.User;
import fr.insee.onyxia.model.catalog.UniversePackage;
import mesosphere.marathon.client.model.v2.App;

/**
 * IDEnforcerTest
 */
@SpringBootTest
public class IDEnforcerTest {
    @Value("${marathon.group.name}")
    private String MARATHON_GROUP_NAME;
    @Autowired
    IDEnforcer idEnforcer;

    @ParameterizedTest
    @CsvSource({ "az/*e-efizea, shelly,internal", "az/*e-efizea, ea/*e/ae-e,datascience" })
    public void shouldChangeServiceIdCloudshell(String userId, String pkgName, String universeId) {
        User user = new User();
        user.setIdep(userId);
        App app = new App();
        UniversePackage pkg = new UniversePackage();
        pkg.setName(pkgName);
        PublishContext context = new PublishContext(universeId);
        idEnforcer.validateContract(app, user, pkg, null, context);
        Pattern pattern = Pattern.compile(MARATHON_GROUP_NAME + "/[a-z0-9]*/[a-z0-9]*-?.*");
        Matcher m = pattern.matcher(app.getId());
        assertTrue(m.matches());
    }

}