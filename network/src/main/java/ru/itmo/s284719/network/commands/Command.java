package ru.itmo.s284719.network.commands;

import java.io.Serializable;

public interface Command extends Serializable {
    String getName();
    String getDesc();
    default String getArgs() { return ""; }
    default int getNumbArgs() { return 0; }
}
