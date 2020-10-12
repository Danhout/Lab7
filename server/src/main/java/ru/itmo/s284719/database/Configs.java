package ru.itmo.s284719.database;

public class Configs {
    protected String dbHost = "localhost";//pg
    protected String dbPort = "5432";
    protected String dbName = "users";//studs
    protected String connectionString = String.format("jdbc:postgresql://%s:%s/%s", dbHost, dbPort, dbName);
}
