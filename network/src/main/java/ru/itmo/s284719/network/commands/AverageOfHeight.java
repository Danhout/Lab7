package ru.itmo.s284719.network.commands;

import java.io.Serializable;

public class AverageOfHeight implements Command, Serializable {

    @Override
    public String getName() {
        return "average_of_height";
    }

    @Override
    public String getDesc() {
        return "вывести среднее значение поля \"height\" для всех элементов коллекции";
    }
}
