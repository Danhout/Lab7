package server;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import database.DatabaseHandler;
import database.User;
import network.Converters;
import network.WaitingOutput;
import network.parser.Pair;
import network.parser.Parser;
import network.space.SpaceMarine;

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
     * The default server's port.
     */
    public static final int DEFAULT_PORT = 8000;

    /**
     * Main function for server.
     *
     * @param args the file's name with collection in format JSON.
     */
    public static void main(String[] args) throws ClassNotFoundException, NoSuchAlgorithmException, IOException {
        // create GSON's parser for format JSON.
        final Gson gson = Converters.registerZoneId(new GsonBuilder()).setPrettyPrinting().create();

        // initialization system's streams of server with auto-flush.
        PrintWriter err = new PrintWriter(
                new OutputStreamWriter(
                        System.err, Charset.forName("UTF-8")), true);
        PrintWriter out = new PrintWriter(
                new OutputStreamWriter(
                        System.out, Charset.forName("UTF-8")), true);
        BufferedReader in = new BufferedReader(
                new InputStreamReader(
                        System.in, Charset.forName("UTF-8")));

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
                err.println("Received the program end symbol.");
                System.exit(0);
            }
            in.reset();
            // else: read line from the console and normalise that.
            String str = Parser.normalise(in.readLine());
            // if (line is empty) than: throw NullPointerException.
            if (str == null || str.equals("")) { throw new NullPointerException(); }
            port = Integer.parseInt(str);
            // else if (the port incorrect) than: throw new IllegalAgrumentException.
            if (port < 1024 || port > 65535) throw new IllegalArgumentException();
        } catch (NullPointerException e) {
            // print default port.
            err.println("Server's default PORT: " + DEFAULT_PORT);
            port = DEFAULT_PORT;
        } catch (IllegalArgumentException e) {
            // print exception and default port.
            err.println("Incorrect PORT");
            err.println("Server's default PORT: " + DEFAULT_PORT);
            port = DEFAULT_PORT;
        } catch (IOException e) {
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
                    String login = null;
                    try {
                        login = in.readLine();
                        if (login == null) {
                            out.println("Received the program end symbol.");
                            System.exit(0);
                        }
                    } catch (Exception e) {
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
                while (adminPassword == null) {
                    out.print("Input admin's password for the database:$");
                    out.flush();
                    String password = null;
                    Console console = System.console();
                    try {
                        if (console == null)
                            password = in.readLine();
                        else
                            password = new String(console.readPassword());

                        if (password == null) {
                            out.println("Received the program end symbol.");
                            System.exit(0);
                        }
                    } catch (Exception e) {
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

                dbHandler = new DatabaseHandler(adminLogin, adminPassword);

                try {
                    dbHandler.dbConnection = dbHandler.getDbConnection();
                } catch (SQLException e) {
                    err.println("PostgresQL database's connection does not exist.");
                    //for logging.
                    //e.printStackTrace();
                }

                while (dbHandler.dbConnection == null) {
                    WaitingOutput.wait("Connect to the database's server");
                    try {
                        dbHandler.getDbConnection();
                    } catch (SQLException e) {/*//for logging.//e.pringStackTrace();*/}
                }

                out.println("Database connection established.");
                out.println("Hello, " + adminLogin + '.');

                // parse Database's data to the collection.
                try {
                    queuePair = dbHandler.getPriorityBlockingQueuePair();
                } catch (ClassNotFoundException e) {
                    err.println("JDBC PostgresQL driver is not found.");
                    err.println("Add org.postgresql:postgresql:42.2.16 library to the project.");
                    // for logging.
                    // e.printStackTrace();
                } catch (SQLException e) {
                    e.printStackTrace();
                }

                while (queuePair == null) {
                    WaitingOutput.wait("Try getting the database's data");
                    try {
                        queuePair = dbHandler.getPriorityBlockingQueuePair();
                    } catch (SQLException e) {/*//for logging.//e.pringStackTrace();*/}
                }

                admin = new User(adminLogin, adminPassword);

            } catch (SQLException e) {
                err.println("Incorrect admin's login or/and password or connection isn't exist.");
                // for logging.
                // e.printStackTrace();
            } catch (ClassNotFoundException e) {
                err.println("PostgresQL database's driver is not found.");
                throw e;
                //for logging.
                //e.printStackTrace();
            }
        }

        // create server.
        new ServerConsole(port, queuePair, dbHandler, admin);
    }
}