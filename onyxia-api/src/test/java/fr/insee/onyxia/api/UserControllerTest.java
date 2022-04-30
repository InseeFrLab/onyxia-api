package fr.insee.onyxia.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import fr.insee.onyxia.api.configuration.BaseTest;
import fr.insee.onyxia.api.configuration.SecurityConfig;
import fr.insee.onyxia.api.configuration.properties.RegionsConfiguration;
import fr.insee.onyxia.api.controller.api.user.UserController;
import fr.insee.onyxia.api.services.utils.HttpRequestUtils;
import fr.insee.onyxia.api.user.OnyxiaUserProvider;
import fr.insee.onyxia.model.OnyxiaUser;
import fr.insee.onyxia.model.User;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

@WebMvcTest(UserController.class)
public class UserControllerTest extends BaseTest {

   @Autowired
   private MockMvc mockMvc;

   @Autowired
   private ObjectMapper mapper;

   @MockBean
   private HttpRequestUtils httpRequestUtils;

   @MockBean
   private RegionsConfiguration regionsConfiguration;

   @MockBean
   private SecurityConfig securityConfig;

   @MockBean
   private OnyxiaUserProvider onyxiaUserProvider;

   @BeforeEach
   public void setUp() {
      User user = new User();
      user.setIdep("XXXXXX");
      user.setGroups(List.of("toto", "tata"));
      Mockito.when(onyxiaUserProvider.getUser(any())).thenReturn(new OnyxiaUser(user));
   }

   @Test
   public void shouldReturnUserInfo() throws Exception {
      MvcResult result = this.mockMvc.perform(get("/user/info"))
      .andExpect(status().isOk())
      .andDo(document("user/info"))
      .andReturn();

      User user = mapper.readValue(result.getResponse().getContentAsString(), User.class);
      assertEquals("XXXXXX", user.getIdep());
      assertTrue(user.getGroups().containsAll(List.of("toto", "tata")));
   }

}
