package basketball.projects.balonAlAire.service;

import java.util.Optional;

import org.springframework.stereotype.Service;

import basketball.projects.balonAlAire.model.User;
import basketball.projects.balonAlAire.repository.UserRepository;

@Service
public class UserService {
    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public Optional<User> getByUsername(String username) {
        return userRepository.findByUsername(username);
    }
}
