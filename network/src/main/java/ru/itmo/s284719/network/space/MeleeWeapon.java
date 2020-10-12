package ru.itmo.s284719.network.space;

import java.io.Serializable;

public enum MeleeWeapon implements Serializable {
    CHAIN_SWORD,
    POWER_SWORD,
    CHAIN_AXE,
    MANREAPER,
    POWER_FIST;

    public static void check(MeleeWeapon meleeWeapon) throws NullPointerException {
        if (meleeWeapon == null) {
            throw new NullPointerException("Значение поля \"Melle Weapon\" не может быть null");
        }
    }
}
