package ru.itmo.s284719.network.commands;

import ru.itmo.s284719.network.space.MeleeWeapon;

import java.io.Serializable;

public class CountGreaterThanMeleeWeapon implements Command, Serializable {
    public MeleeWeapon meleeWeapon;

    public CountGreaterThanMeleeWeapon(MeleeWeapon meleeWeapon) {
        this.meleeWeapon = meleeWeapon;
    }

    @Override
    public String getName() {
        return "count_greater_than_melee_weapon";
    }

    @Override
    public String getDesc() {
        return "вывести количество элементов, " +
                "значение поля meleeWeapon которых больше заданного";
    }

    @Override
    public String getArgs() {
        return "meleeWeapon";
    }

    @Override
    public int getNumbArgs() {
        return 1;
    }
}
