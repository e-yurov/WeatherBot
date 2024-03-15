package ru.yurov.testspringtelegrambot.exceptions;

public class ServerException extends Exception {
    public ServerException() {
        super("Произошла ошибка на сервере.");
    }
}
