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
        user.setAccountId("kjwang24");
        userRepository.save(user);

        Optional<User> found = userRepository.findByAccountId("kjwang24");

        assertThat(found).isPresent();
    }

    @Test
    void findByUsername_returnsEmpty_whenNoMatch() {
        Optional<User> found = userRepository.findByAccountId("doesNotExist");

        assertThat(found).isEmpty();
    }

}
