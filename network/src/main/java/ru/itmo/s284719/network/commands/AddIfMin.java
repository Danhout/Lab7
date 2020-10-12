package ru.itmo.s284719.network.commands;

import ru.itmo.s284719.network.space.SpaceMarine;

import java.io.Serializable;

public class AddIfMin implements Command, Serializable {
    public SpaceMarine spaceMarine;

    public AddIfMin(SpaceMarine spaceMarine) {
        this.spaceMarine = spaceMarine;
    }

    @Override
    public String getName() {
        return "add_if_min";
    }

    @Override
    public String getDesc() {
        return "добавить новый элемент в коллекцию, если его " +
                "значение меньше, чем у наименьшего " +
                "элемента этой коллекции";
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
