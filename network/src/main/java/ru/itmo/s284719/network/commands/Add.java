package ru.itmo.s284719.network.commands;

import ru.itmo.s284719.network.space.SpaceMarine;

import java.io.Serializable;

public class Add implements Command, Serializable {
    public SpaceMarine spaceMarine;

    public Add(SpaceMarine spaceMarine) {
        this.spaceMarine = spaceMarine;
    }

    @Override
    public String getName() {
        return "add";
    }

    @Override
    public String getDesc() {
        return "добавить новый элемент в коллекцию";
    }

    @Override
    public String getArgs() {
        return "{element}";
    }

    @Override
    public int getNumbArgs() {
        return 1;
    }
}
