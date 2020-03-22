package fr.insee.onyxia.api.controller.api.user;

import fr.insee.onyxia.api.services.SSHService;
import fr.insee.onyxia.api.services.UserDataService;
import fr.insee.onyxia.api.services.UserProvider;
import fr.insee.onyxia.model.User;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

@Tag(name = "User",description = "Personal data")
@RequestMapping("/user")
@RestController
public class UserController {

   @Autowired
   private UserProvider userProvider;

   @Autowired
   private UserDataService userDataService;

   @Autowired
   private SSHService sshService;

   @GetMapping("/info")
   public User userInfo(HttpServletRequest request) {
      User user = userProvider.getUser();
      userDataService.fetchUserData(user);
      user.setIp(HttpReqRespUtils.getClientIpAddressIfServletRequestExist(request));

      return user;
   }

   @GetMapping("/update")
   public User updateUser(HttpServletRequest request) throws Exception {
      User user = userProvider.getUser();
      user.setIp(HttpReqRespUtils.getClientIpAddressIfServletRequestExist(request));
      sshService.updateSsh(user);

      return user;
   }

   public static class HttpReqRespUtils {

      private static final String[] IP_HEADER_CANDIDATES = {
              "X-Forwarded-For",
              "Proxy-Client-IP",
              "WL-Proxy-Client-IP",
              "HTTP_X_FORWARDED_FOR",
              "HTTP_X_FORWARDED",
              "HTTP_X_CLUSTER_CLIENT_IP",
              "HTTP_CLIENT_IP",
              "HTTP_FORWARDED_FOR",
              "HTTP_FORWARDED",
              "HTTP_VIA",
              "REMOTE_ADDR"
      };

      public static String getClientIpAddressIfServletRequestExist(HttpServletRequest request) {

         for (String header: IP_HEADER_CANDIDATES) {
            String ipList = request.getHeader(header);
            if (ipList != null && ipList.length() != 0 && !"unknown".equalsIgnoreCase(ipList)) {
               String ip = ipList.split(",")[0];
               return ip;
            }
         }

         return request.getRemoteAddr();
      }
   }
}
