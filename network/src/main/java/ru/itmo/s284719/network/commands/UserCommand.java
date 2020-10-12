package ru.itmo.s284719.network.commands;

import java.io.Serializable;

public class UserCommand implements Serializable {
    private Object command;
    private String login;
    private String password;

    public UserCommand(Command command, String login, String password) {
        this.command = command;
        this.login = login;
        this.password = password;
    }

    public Object getCommand() {
        return command;
    }

    public String getLogin() {
        return login;
    }

    public String getPassword() {
        return password;
    }
}
