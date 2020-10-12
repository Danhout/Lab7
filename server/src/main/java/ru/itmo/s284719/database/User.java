package ru.itmo.s284719.database;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class User {
    private String login;
    private byte[] hash_password;

    public User(String login, String password) throws NoSuchAlgorithmException {
        this.login = login;
        this.hash_password = MessageDigest.getInstance("SHA-1").digest(password.getBytes());
    }

    public String getLogin() {
        return login;
    }

    public byte[] getHashPassword() {
        return hash_password;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public void setPassword(String password) throws NoSuchAlgorithmException {
        this.hash_password = MessageDigest.getInstance("SHA-1").digest(password.getBytes());
    }
}
