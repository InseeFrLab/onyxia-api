package fr.insee.onyxia.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.contains;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import com.fasterxml.jackson.databind.ObjectMapper;

import fr.insee.onyxia.api.configuration.BaseTest;
import fr.insee.onyxia.api.controller.api.user.UserController;
import fr.insee.onyxia.api.services.SSHService;
import fr.insee.onyxia.api.services.UserDataService;
import fr.insee.onyxia.model.User;

@WebMvcTest(UserController.class)
public class UserControllerTest extends BaseTest {

   @Autowired
   private MockMvc mockMvc;

   @MockBean
   private UserDataService userDataService;

   @MockBean
   private SSHService sshService;

   @Autowired
   private ObjectMapper mapper;

   @Test
   public void shouldReturnUserInfo() throws Exception {
      MvcResult result = this.mockMvc.perform(get("/user/info"))
      .andExpect(status().isOk())
      .andDo(document("user/info"))
      .andReturn();

      User user = mapper.readValue(result.getResponse().getContentAsString(), User.class);
      assertEquals(user.getIdep(), "XXXXXX");
   }

}
