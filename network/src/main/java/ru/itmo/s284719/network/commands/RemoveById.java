package ru.itmo.s284719.network.commands;

import java.io.Serializable;

public class RemoveById implements Command, Serializable {
    public int id;

    public RemoveById(int id) {
        this.id = id;
    }


    @Override
    public String getName() {
        return "remove_by_id";
    }

    @Override
    public String getDesc() {
        return "удалить элемент из коллекции по его id";
    }

    @Override
    public String getArgs() {
        return "id";
    }

    @Override
    public int getNumbArgs() {
        return 1;
    }
}
