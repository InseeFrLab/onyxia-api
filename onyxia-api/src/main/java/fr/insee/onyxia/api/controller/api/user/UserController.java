package fr.insee.onyxia.api.controller.api.user;

import fr.insee.onyxia.api.services.UserProvider;
import fr.insee.onyxia.model.User;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "User",description = "Personal data")
@RequestMapping("/user")
@RestController
@SecurityRequirement(name="auth")
public class UserController {

   @Autowired
   private UserProvider userProvider;

   @GetMapping("/info")
   public User userInfo() {
      return userProvider.getUser();
   }
}
