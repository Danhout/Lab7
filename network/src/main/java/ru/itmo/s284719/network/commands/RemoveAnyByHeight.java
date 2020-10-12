package ru.itmo.s284719.network.commands;

import java.io.Serializable;

public class RemoveAnyByHeight implements Command, Serializable {
    public Integer height;

    public RemoveAnyByHeight(Integer height) {
        this.height = height;
    }

    @Override
    public String getName() {
        return "remove_any_by_height";
    }

    @Override
    public String getDesc() {
        return "удалить из коллекции один элемент, значение поля " +
                "height которого эквивалентно заданному";
    }

    @Override
    public String getArgs() {
        return "height";
    }

    @Override
    public int getNumbArgs() {
        return 1;
    }
}
