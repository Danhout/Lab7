package ru.itmo.s284719.database;

public class Configs {
    protected String dbHost = "pg";//pg localhost
    protected String dbPort = "5432";
    protected String dbName = "studs";//studs users
    protected String connectionString = String.format("jdbc:postgresql://%s:%s/%s", dbHost, dbPort, dbName);
}
