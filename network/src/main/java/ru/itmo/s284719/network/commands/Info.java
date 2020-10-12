package ru.itmo.s284719.network.commands;

import java.io.Serializable;

public class Info implements Command, Serializable {

    @Override
    public String getName() {
        return "info";
    }

    @Override
    public String getDesc() {
        return "вывести в стандартный поток вывода информацию о коллекции " +
                "(тип, дата инициализации, количество элементов и т.д.)";
    }
}
