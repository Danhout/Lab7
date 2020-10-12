package ru.itmo.s284719.network.space;

import java.io.Serializable;

public enum AstartesCategory implements Serializable {
    ASSAULT,
    CHAPLAIN,
    HELIX;

    public static void check(AstartesCategory category) throws NullPointerException {
        if (category == null) {
            throw new NullPointerException("Значение поля \"Category\" не может быть null");
        }
    }
}
