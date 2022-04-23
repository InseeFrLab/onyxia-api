package fr.insee.onyxia.api.controller.api.user;

import fr.insee.onyxia.api.user.OnyxiaUserProvider;
import fr.insee.onyxia.model.OnyxiaUser;
import fr.insee.onyxia.model.region.Region;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "User",description = "Personal data")
@RequestMapping(value={"/api/user", "/user"})
@RestController
@SecurityRequirement(name="auth")
public class UserController {

   @Autowired
   private OnyxiaUserProvider userProvider;

   @GetMapping("/info")
   public OnyxiaUser userInfo(@Parameter(hidden = true) Region region) {
      return userProvider.getUser(region);
   }

}
