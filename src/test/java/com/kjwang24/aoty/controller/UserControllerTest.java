package com.kjwang24.aoty.controller;

import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.oauth2Login;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.kjwang24.aoty.entity.User;
import com.kjwang24.aoty.repository.UserRepository;

@WebMvcTest(UserController.class)
public class UserControllerTest {

    @MockitoBean
    private UserRepository userRepository;

    @Autowired
    private MockMvc mockMvc;

    @Test
    void getCurrentUser_returnsDisplayName() throws Exception {
        User user = new User();
        user.setAccountId("kjwang24");
        user.setDisplayName("Katherine");

        when(userRepository.findByAccountId("kjwang24")).thenReturn(Optional.of(user));

        mockMvc.perform(get("/me")
               .with(oauth2Login().attributes(attrs -> attrs.put("account_id", "kjwang24"))))
               .andExpect(status().isOk())
               .andExpect(content().json("{\"display_name\":\"Katherine\"}"));
    }

}
