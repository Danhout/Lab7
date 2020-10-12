package ru.itmo.s284719.network.commands;

import ru.itmo.s284719.network.space.SpaceMarine;

import java.io.Serializable;

public class RemoveGreater implements Command, Serializable {
    public SpaceMarine spaceMarine;

    public RemoveGreater(SpaceMarine spaceMarine) {
        this.spaceMarine = spaceMarine;
    }

    @Override
    public String getName() {
        return "remove_greater";
    }

    @Override
    public String getDesc() {
        return "удалить из коллекции все элементы, " +
                "превышающие заданный";
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
