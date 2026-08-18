package com.kjwang24.aoty.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;

import com.kjwang24.aoty.entity.User;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Test
    void findByUsername_returnsSavedUser() {
        User user = new User();
        user.setUsername("kjwang24");
        user.setEmail("kjwang24@spring.boot");
        userRepository.save(user);

        Optional<User> found = userRepository.findByUsername("kjwang24");

        assertThat(found).isPresent();
        assertThat(found.get().getEmail()).isEqualTo("kjwang24@spring.boot");
    }

    @Test
    void findByUsername_returnsEmpty_whenNoMatch() {
        Optional<User> found = userRepository.findByUsername("doesNotExist");

        assertThat(found).isEmpty();
    }

}
