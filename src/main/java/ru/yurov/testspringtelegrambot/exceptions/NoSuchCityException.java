package ru.yurov.testspringtelegrambot.exceptions;

public class NoSuchCityException extends Exception {
    public NoSuchCityException(String message) {
        super(message);
    }
}
