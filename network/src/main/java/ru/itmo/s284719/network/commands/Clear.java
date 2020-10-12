package ru.itmo.s284719.network.commands;

import java.io.Serializable;

public class Clear implements Command, Serializable {

    @Override
    public String getName() {
        return "clear";
    }

    @Override
    public String getDesc() {
        return "очистить коллекцию";
    }
}
