package ru.itmo.s284719.network.commands;

import ru.itmo.s284719.network.space.SpaceMarine;

import java.io.Serializable;

public class Update implements Command, Serializable {
    public int id;
    public SpaceMarine newSpaceMarine;

    public Update(int id, SpaceMarine newSpaceMarine) {
        this.id = id;
        this.newSpaceMarine = newSpaceMarine;
    }

    @Override
    public String getName() {
        return "update";
    }

    @Override
    public String getDesc() {
        return "обновить значение элемента коллекции, id которого равен заданному";
    }

    @Override
    public String getArgs() {
        return "id, {element}";
    }

    @Override
    public int getNumbArgs() {
        return 2;
    }
}
