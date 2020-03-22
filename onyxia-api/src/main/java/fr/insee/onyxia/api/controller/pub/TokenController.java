package fr.insee.onyxia.api.controller.pub;

import org.keycloak.TokenVerifier;
import org.keycloak.common.VerificationException;
import org.keycloak.representations.AccessToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Public")
@RestController
@RequestMapping("/public")
@Tag(name="Public")
public class TokenController {
   
   @GetMapping("/token")
   @Operation
   public AccessToken greeting(@RequestParam("token") String tokenString) {
      AccessToken token = verifToken(tokenString);
      return token;
   }
   
   private AccessToken verifToken(String tokenString) {
      try {
         AccessToken token = TokenVerifier.create(tokenString, AccessToken.class).getToken();
         return token;
      }
      catch (VerificationException e) {
         e.printStackTrace();
         return null;
      }
   }
   
}

