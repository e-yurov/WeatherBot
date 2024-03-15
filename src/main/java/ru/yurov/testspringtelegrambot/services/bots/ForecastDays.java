package ru.yurov.testspringtelegrambot.services.bots;

public enum ForecastDays {
    ONE(1),
    TWO(2),
    THREE(3),
    FOUR(4),
    FIVE(5);

    private final int value;

    ForecastDays(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    @Override
    public String toString() {
        return "FORECAST_" + this.name();
    }
}
