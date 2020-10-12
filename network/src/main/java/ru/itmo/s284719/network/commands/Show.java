package ru.itmo.s284719.network.commands;

import java.io.Serializable;

public class Show implements Command, Serializable {

    @Override
    public String getName() {
        return "show";
    }

    @Override
    public String getDesc() {
        return "вывести в стандартный поток вывода все " +
                "элементы коллекции в строковом представлении";
    }
}
