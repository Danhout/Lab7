package ru.itmo.s284719.network.commands;

import java.io.Serializable;

public class RemoveHead implements Command, Serializable {

    @Override
    public String getName() {
        return "remove_head";
    }

    @Override
    public String getDesc() {
        return "вывести первый элемент коллекции и удалить его";
    }
}
