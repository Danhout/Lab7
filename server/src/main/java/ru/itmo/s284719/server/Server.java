package ru.itmo.s284719.server;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import ru.itmo.s284719.database.DatabaseHandler;
import ru.itmo.s284719.database.User;
import ru.itmo.s284719.network.Converters;
import ru.itmo.s284719.network.WaitingOutput;
import ru.itmo.s284719.network.parser.Pair;
import ru.itmo.s284719.network.parser.Parser;
import ru.itmo.s284719.network.space.SpaceMarine;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.nio.charset.Charset;
import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;
import java.util.concurrent.PriorityBlockingQueue;

/**
 * Server's main class.
 *
 * @version 0.3
 * @author Danhout.
 */
public class Server {
    /**
     * Log4j 2 logger.
     */
    private static final Logger logger = LogManager.getLogger(Server.class);
    /**
     * The default server's port.
     */
    public static final int DEFAULT_PORT = 8000;
    /**
     * The GSON's parser for format JSON.
     */
    private static final Gson gson = Converters.registerZoneId(new GsonBuilder()).setPrettyPrinting().create();
    /*
     * System error's streams of server with auto-flush.
     */
    private static final PrintWriter err = new PrintWriter(
            new OutputStreamWriter(
                    System.err, Charset.forName("UTF-8")), true);
    /*
     * System output's streams of server with auto-flush.
     */
    private static final PrintWriter out = new PrintWriter(
            new OutputStreamWriter(
                    System.out, Charset.forName("UTF-8")), true);
    /*
     * System input's streams of server with auto-flush.
     */
    private static final BufferedReader in = new BufferedReader(
            new InputStreamReader(
                    System.in, Charset.forName("UTF-8")));

    /**
     * Main function for server.
     *
     * @param args the file's name with collection in format JSON.
     */
    public static void main(String[] args) throws ClassNotFoundException, NoSuchAlgorithmException, IOException {

        // declaration server's PORT.
        int port;

        // initialization PORT for the server and start working of the server.
        try {
            // print info for input server's PORT.
            out.println("Server's PORT:");
            out.print("$");
            out.flush();

            // if (received end symbol) than: print the information and exit the program.
            in.mark(1);
            if (in.read() == -1) {
                // logging
                logger.info("Received the end symbol.");
                err.println("Received the program end symbol.");
                System.exit(0);
            }
            in.reset();
            // else: read line from the console and normalise that.
            String str = Parser.normalise(in.readLine());
            // logging.
            logger.warn("Input port's string: \"" + str + "\".");
            // if (line is empty) than: throw NullPointerException.
            if (str.equals(""))
                throw new NullPointerException();
            port = Integer.parseInt(str);
            // else if (the port incorrect) than: throw new IllegalArgumentException.
            if (port < 1024 || port > 65535)
                throw new IllegalArgumentException();
        } catch (NullPointerException e) {
            // logging.
            logger.error("Use default port: " + DEFAULT_PORT + '.');
            // print default port.
            out.println("Server's default PORT: " + DEFAULT_PORT);
            port = DEFAULT_PORT;
        } catch (IllegalArgumentException e) {
            // logging.
            logger.error("Incorrect format port.");
            logger.error("Use default port: " + DEFAULT_PORT + '.');
            // print exception and default port.
            err.println("Incorrect PORT");
            out.println("Server's default PORT: " + DEFAULT_PORT);
            port = DEFAULT_PORT;
        } catch (IOException e) {
            // logging.
            logger.fatal("Fatal exception.", e);
            // if (received IOException) than: print that and exit from the program.
            err.println(e.getMessage());
            return;
        }

        User admin = null;
        // declare server's collection (PriorityQueue<SpaceMarine>).
        PriorityBlockingQueue<Pair<SpaceMarine, String>> queuePair = null;
        DatabaseHandler dbHandler = null;

        // cycle, while the admin's data incorrect, database is disconnected or driver isn't exists.
        while (admin == null) {
            try {
                String adminLogin = null, adminPassword = null;
                while (adminLogin == null) {
                    out.print("Input admin's login for the database:$");
                    out.flush();
                    String login = in.readLine();
                    // logging.
                    logger.warn("Input login's string: \"" + login + "\".");
                    if (login == null) {
                        // logging.
                        logger.info("Received the end symbol.");
                        out.println("Received the program end symbol.");
                        System.exit(0);
                    }
                    if (login.length() == 0)
                        err.println("Login needs to be not empty.");
                    else if (login.length() > 32)
                        err.println("Login needs to have less or equals than 32 characters.");
                    else if (login.matches("^[^a-zA-Z]+.*"))
                        err.println("Login needs to starting Latin character.");
                    else if (!login.matches("^[a-zA-Z]+(_?[a-zA-Z0-9])*_?"))
                        err.println("Login needs to have only Latin characters, digits and underlines between Latin characters or digits.");
                    else
                        adminLogin = login;
                }
                // logging.
                logger.info("Correct login's format.");

                while (adminPassword == null) {
                    out.print("Input admin's password for the database:$");
                    out.flush();
                    String password = null;
                    Console console = System.console();
                    if (console == null) {
                        // logging.
                        logger.info("Input invisibility mode for password is disabled");
                        password = in.readLine();
                    } else
                        password = new String(console.readPassword());

                    if (password == null) {
                        // logging.
                        logger.info("Received the end symbol.");
                        out.println("Received the program end symbol.");
                        System.exit(0);
                    }
                    if (password.length() == 0)
                        err.println("Password needs to be not empty.");
                    else if (password.length() > 32)
                        err.println("Password needs to have less or equals than 32 characters.");
                    else if (password.matches(".*\\W.*"))
                        err.println("Password needs to have only Latin characters, digits and underlines between Latin characters or digits.");
                    adminPassword = password;
                }
                // logging.
                logger.info("Correct password's format.");

                dbHandler = new DatabaseHandler(adminLogin, adminPassword);

                try {
                    dbHandler.dbConnection = dbHandler.getDbConnection();
                } catch (SQLException e) {
                    // logging.
                    logger.error("PostgresQL database's connection doesn't exist.", e);
                    err.println("PostgresQL database's connection does not exist.");
                }

                while (dbHandler.dbConnection == null) {
                    // logging.
                    logger.warn("Try to connection to the database's server.");
                    WaitingOutput.wait("Connect to the database's server");
                    try {
                        dbHandler.getDbConnection();
                    } catch (SQLException e) {
                        // logging.
                        logger.error("Connection is incorrect.", e);
                    }
                }

                // logger.
                logger.info("Admin: \"" + adminLogin + "\" connect to the database.");
                out.println("Database connection established.");
                out.println("Hello, " + adminLogin + '.');

                // parse Database's data to the collection.
                try {
                    queuePair = dbHandler.getPriorityBlockingQueuePair();
                } catch (ClassNotFoundException e) {
                    err.println("JDBC PostgresQL driver is not found.");
                    err.println("Adding org.postgresql:postgresql:42.2.16 library to the project is fatal.");
                    // logging.
                    logger.error("JDBC PostgresQL driver is not found.", e);
                } catch (SQLException e) {
                    //err.println("SQLException: " + e.getMessage());
                    // logging.
                    logger.error("Getting collection from the database is incorrect.", e);
                }

                while (queuePair == null) {
                    // logging.
                    logger.warn("Try to getting the collection from the database again.");
                    WaitingOutput.wait("Try getting the database's data");
                    try {
                        queuePair = dbHandler.getPriorityBlockingQueuePair();
                    } catch (SQLException e) {
                        //err.println("SQLException: " + e.getMessage());
                        // logging.
                        logger.error("Getting collection from the database is incorrect.", e);
                    }
                }

                admin = new User(adminLogin, adminPassword);
                // logging.
                logger.info("The admin connect to the database.");

            } catch (SQLException e) {
                err.println("Incorrect admin's login or/and password or connection isn't exist.");
                // logging.
                logger.error("Incorrect admin's login or/and password.", e);
            } catch (ClassNotFoundException e) {
                err.println("PostgresQL database's driver is not found.");
                // logging.
                logger.error("PostgresQL database's driver is not found.", e);
                throw e;
            }
        }

        // create ru.itmo.s284719.server.
        new ServerConsole(port, queuePair, dbHandler, admin);
    }
}