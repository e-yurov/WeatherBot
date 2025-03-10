package ru.yurov.testspringtelegrambot.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.yurov.testspringtelegrambot.models.User;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

}
