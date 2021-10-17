package htwb.ai.authservice.controller;

import com.fasterxml.jackson.databind.json.JsonMapper;
import htwb.ai.authservice.entity.User;
import htwb.ai.authservice.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
class UserControllerTest {
    private MockMvc mockMvc;
    private UserService serviceMock;

    @BeforeEach
    public void setup() {
        serviceMock = Mockito.mock(UserService.class);
        mockMvc = MockMvcBuilders.standaloneSetup(
                new UserController(serviceMock)).build();
    }

    @Test
    void loginShouldReturnOk() throws Exception {
        String userId = "testUser";
        String password = "password";
        String authToken = UUID.randomUUID().toString();
        when(serviceMock.authenticate(userId, password)).thenReturn(authToken);
        mockMvc.perform(post("/auth")
                .contentType("application/json")
                .content("{\"userId\":\"" + userId + "\",\"password\":\"" + password + "\"}"))
                .andExpect(status().isOk());
    }

    @Test
    void loginWithInvalidCredentialsShouldReturnUnauthorized() throws Exception {
        String userId = "invalidUser";
        String password = "password";
        when(serviceMock.authenticate(userId, password)).thenReturn(null);
        mockMvc.perform(post("/auth")
                .contentType("application/json")
                .content("{\"userId\":\"" + userId + "\",\"password\":\"" + password + "\"}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void loginWithEmptyCredentialsShouldReturnUnauthorized() throws Exception {
        String userId = "invalidUser";
        String password = "password";
        when(serviceMock.authenticate(userId, password)).thenReturn(null);
        mockMvc.perform(post("/auth")
                .contentType("application/json")
                .content("{}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void getUserByAuthTokenShouldReturnOkAndUser() throws Exception {
        User testUser = new User();
        testUser.setUserId("testUser");
        String testAuthToken = "testAuthToken";
        when(serviceMock.findUserByAuthToken(testAuthToken)).thenReturn(Optional.of(testUser));
        ResultActions resultActions = mockMvc.perform(get("/auth?auth_token=" + testAuthToken)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
        MvcResult result = resultActions.andReturn();
        String contentAsString = result.getResponse().getContentAsString(StandardCharsets.UTF_8);
        JsonMapper jsonMapper = new JsonMapper();
        User user = jsonMapper.readValue(contentAsString, User.class);
        assertNotNull(user);
        assertEquals(testUser.getUserId(), user.getUserId());
    }

    @Test
    void getUserByAuthTokenShouldReturnOkAndEmpty() throws Exception {
        String testAuthToken = "invalidAuthToken";
        when(serviceMock.findUserByAuthToken(testAuthToken)).thenReturn(Optional.empty());
        ResultActions resultActions = mockMvc.perform(get("/auth?auth_token=" + testAuthToken)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
        MvcResult result = resultActions.andReturn();
        String contentAsString = result.getResponse().getContentAsString(StandardCharsets.UTF_8);
        assertTrue(contentAsString.isEmpty());
    }

    @Test
    void getUserByIdShouldReturnOkAndUser() throws Exception {
        User testUser = new User();
        testUser.setUserId("testUser");
        String testAuthToken = "testAuthToken";
        when(serviceMock.findUserByAuthToken(testAuthToken)).thenReturn(Optional.of(testUser));
        when(serviceMock.findUserByUserId(testUser.getUserId())).thenReturn(Optional.of(testUser));
        ResultActions resultActions = mockMvc.perform(get("/auth/" + testUser.getUserId())
                .header("Authorization", testAuthToken)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
        MvcResult result = resultActions.andReturn();
        String contentAsString = result.getResponse().getContentAsString(StandardCharsets.UTF_8);
        JsonMapper jsonMapper = new JsonMapper();
        User user = jsonMapper.readValue(contentAsString, User.class);
        assertNotNull(user);
        assertEquals(testUser.getUserId(), user.getUserId());
    }

    @Test
    void getUserByIdInvalidTokenShouldReturnUnauthorized() throws Exception {
        User testUser = new User();
        testUser.setUserId("testUser");
        String testAuthToken = "invalidAuthToken";
        when(serviceMock.findUserByAuthToken(testAuthToken)).thenReturn(Optional.empty());
        when(serviceMock.findUserByUserId(testUser.getUserId())).thenReturn(Optional.of(testUser));
        mockMvc.perform(get("/auth/" + testUser.getUserId())
                .header("Authorization", testAuthToken)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void getUserByIdWithNonExistingShouldReturnNotFound() throws Exception {
        User testUser = new User();
        testUser.setUserId("testUser");
        String testAuthToken = "testAuthToken";
        when(serviceMock.findUserByAuthToken(testAuthToken)).thenReturn(Optional.of(testUser));
        when(serviceMock.findUserByUserId("InvalidId")).thenReturn(Optional.empty());
        mockMvc.perform(get("/auth/" + testUser.getUserId())
                .header("Authorization", testAuthToken)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }
}