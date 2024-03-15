package ru.yurov.testspringtelegrambot.services.database;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yurov.testspringtelegrambot.models.User;
import ru.yurov.testspringtelegrambot.repositories.UserRepository;

import java.util.List;
import java.util.Optional;

@Service
@Transactional(readOnly = true)
public class UserService {
    private final UserRepository userRepository;

    @Autowired
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public Optional<User> getUserByChatId(long chatId) {
        return userRepository.findById(chatId);
    }

    @Transactional
    public void saveUser(User user) {
        userRepository.save(user);
    }
}
