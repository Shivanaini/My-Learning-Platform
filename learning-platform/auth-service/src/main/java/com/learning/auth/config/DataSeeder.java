package com.learning.auth.config;

import com.learning.auth.model.Role;
import com.learning.auth.model.User;
import com.learning.auth.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/** Creates two demo accounts on first start so you can log in immediately. */
@Component
public class DataSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataSeeder.class);

    private final UserRepository users;
    private final PasswordEncoder encoder;

    public DataSeeder(UserRepository users, PasswordEncoder encoder) {
        this.users = users;
        this.encoder = encoder;
    }

    @Override
    public void run(String... args) {
        seed("instructor", "Demo Instructor", "instructor@learning.local", "instructor123", Role.INSTRUCTOR);
        seed("student", "Demo Student", "student@learning.local", "student123", Role.STUDENT);
    }

    private void seed(String username, String name, String email, String rawPassword, Role role) {
        if (!users.existsByUsername(username)) {
            users.save(new User(username, name, email, encoder.encode(rawPassword), role));
            log.info("Seeded demo user '{}' ({})", username, role);
        }
    }
}
