package ru.itmo.s284719.client;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.itmo.s284719.network.*;
import ru.itmo.s284719.network.commands.*;
import ru.itmo.s284719.network.parser.Pair;
import ru.itmo.s284719.network.space.MeleeWeapon;
import ru.itmo.s284719.network.space.SpaceMarine;

import java.io.Console;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.nio.channels.SocketChannel;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

/**
 * Client's class with really commands.
 *
 * @version 0.4
 * @author Danhout.
 */
public class ClientConsole extends SimpleCMD {
    /**
     * Log4j 2 logger.
     */
    private static final Logger logger = LogManager.getLogger(ClientConsole.class);
    /**
     * The server's IP.
     */
    private final String IP;
    /**
     * The server's PORT.
     */
    private final int PORT;
    /**
     * The client's channel.
     */
    private SocketChannel channel;
    /**
     * The field with information about connection.
     */
    public boolean wasConnected = false;
    /**
     * The user's login.
     */
    private String login = null;
    /**
     * The user's password.
     */
    private String password = null;

    /**
     * Client's constructor.
     *
     * @param address the server's IP.
     * @param port the server's PORT.
     */
    public ClientConsole(String address, int port) throws UnknownHostException {
        // initialise server's IP and PORT.
        IP = InetAddress.getByName(address).getHostAddress();
        PORT = port;

        // connect to server.
        connectionToServer();
    }

    private void connectionToServer() {
        while (true) {
            try {
                channel = SocketChannel.open(new InetSocketAddress(IP, PORT));
                channel.configureBlocking(false);
                if (channel == null) {
                    throw new IOException("The server is disconnected.");
                }
                // logging.
                logger.error("Connected to server with IP: " + IP + ", PORT: " + PORT + '.');
                out.println("\u001B[32m" + "Connected to server with IP: " + IP + ", Port: " + PORT + "..." + "\u001B[0m");
                wasConnected = true;

                if (login == null || password == null)
                    registerOrAuthorizerUser();

                do {
                    // execute command from the server's console.
                    runCommand(readConsoleLine());
                } while (true);
            } catch (IOException e) {
                // logging.
                logger.error("IOException in command or connection.", e);
                // if (creating is fail) than: create new client.
                if (wasConnected) {
                    // logging.
                    logger.error("Server with IP: " + IP + ", PORT: " + PORT + " is unavailable.");
                    err.println("Server with IP: " + IP + ", Port: " + PORT + " is unavailable.");
                }
                wasConnected = false;
                while (channel == null || !channel.isConnected()) {
                    WaitingOutput.wait("Attempt to connect to the server");
                    try {
                        channel = SocketChannel.open(new InetSocketAddress(IP, PORT));
                        channel.configureBlocking(false);
                    } catch (IOException ioE) {
                        // logging.
                        logger.error("New connection to server with IP: " + IP + ", PORT: " + PORT + " is fail");
                    }
                }
            } catch (NoSuchAlgorithmException | ClassNotFoundException e) {
                // logging.
                logger.fatal(e.getMessage(), e);
            }
        }
    }


    public void checkConnection() throws IOException {
        if (!channel.isConnected()) {
            throw new IOException("Server with IP: " + IP + ", Port: " + PORT + " is unavailable.");
        } else if (!wasConnected) {
            // logging.
            logger.error("Connected to server with IP: " + IP + ", PORT: " + PORT + '.');
            out.println("\n\u001B[32m" + "Connected to server with IP: " + IP + ", Port: " + PORT + "..." + "\u001B[0m");
        } else {
            wasConnected = true;
        }
    }

    public void registerOrAuthorizerUser() throws IOException, NoSuchAlgorithmException, ClassNotFoundException {
        out.println("Do you want to r[egister] or a[uthorizer]:");
        String res = readConsoleLine().toLowerCase();
        if (res.matches("r|register")) {
            // logging.
            logger.warn("Try register user.");
            registerUser();
        } else if (res.matches("a|authorizer")) {
            // logging.
            logger.warn("Try authorizer user.");
            authorizerUser();
        } else {
            err.println("You need to write r/a or register/authorizer.");
            registerOrAuthorizerUser();
        }
    }

    public void registerUser() throws IOException, ClassNotFoundException {
        while (login == null) {
            out.print("Input your login:$");
            out.flush();
            String login = in.readLine();
            // logging.
            logger.warn("Input user's login: \"" + login + "\".");
            if (login == null) {
                // logging
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
            else if (!login.matches("^[a-zA-Z]+(_?[a-zA-Z0-9])*"))
                err.println("Login needs to have only Latin characters, digits and underlines between Latin characters or digits.");
            else {
                // logging.
                logger.info("Send user's login to server with IP: " + IP + ", PORT: " + PORT + " for registering.");
                ObjectSocketChannel.sendObject(channel, new UserCommand(null, login, null));
                Boolean isFreeLogin = (Boolean) ObjectSocketChannel.getObject(channel);
                if (isFreeLogin) {
                    // logging.
                    logger.info("Login: " + login + " is free for registering.");
                    this.login = login;
                } else {
                    // logging.
                    logger.info("Login: " + login + " isn't free for registering.");
                    err.println("The login isn't free.");
                }
            }
        }
        while (password == null) {
            out.print("Input your password:$");
            out.flush();
            String password;
            Console console = System.console();
            if (console == null) {
                // logging.
                logger.info("Input invisibility mode for password is disabled");
                password = in.readLine();
            } else {
                password = new String(console.readPassword());
            }

            if (password == null) {
                // logging
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
            else {
                // logging.
                logger.info("Send user's full data to server with IP: " + IP + ", PORT: " + PORT + " for registering.");
                ObjectSocketChannel.sendObject(channel, new UserCommand(null, login, password));
                ObjectSocketChannel.getObject(channel);
                this.password = password;
            }
        }
        // logging.
        logger.info("User: " + login + " is registered.");
        out.println("Hello, " + login + "!");
    }

    public void authorizerUser() throws IOException, ClassNotFoundException {
        while (login == null) {
            out.print("Input your login:$");
            out.flush();
            String login = in.readLine();
            // logging.
            logger.warn("Input user's login: \"" + in + "\".");
            if (login == null) {
                // logging
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
            else {
                // logging.
                logger.info("Send user's login to server with IP: " + IP + ", PORT: " + PORT + " for logging.");
                ObjectSocketChannel.sendObject(channel, new UserCommand(null, login, null));
                Boolean isFreeLogin = (Boolean) ObjectSocketChannel.getObject(channel);
                if (!isFreeLogin) {
                    // logging.
                    logger.info("Login: " + login + " is exist.");
                    this.login = login;
                } else {
                    // logging.
                    logger.info("Login: " + login + " isn't exist.");
                    err.println("The login isn't exist.");
                }
            }
        }
        while (password == null) {
            out.print("Input your password:$");
            out.flush();
            String password;
            Console console = System.console();
            if (console == null) {
                // logging.
                logger.info("Input invisibility mode for password is disabled");
                password = in.readLine();
            } else {
                password = new String(console.readPassword());
            }

            if (password == null) {
                // logging
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
            else {
                // logging.
                logger.info("Send user's full data to server with IP: " + IP + ", PORT: " + PORT + " for logging.");
                ObjectSocketChannel.sendObject(channel, new UserCommand(null, login, password));
                Boolean isCorrectPassword = (Boolean) ObjectSocketChannel.getObject(channel);
                if (isCorrectPassword) {
                    this.password = password;
                } else {
                    // logging.
                    logger.info("Incorrect user's password.");
                    err.println("Incorrect password.");
                }
            }
        }
        // logging.
        logger.info("User: " + login + " is authorized.");
        out.println("Hello, " + login + "!");
    }

    /**
     * Output information about the collection to the standard output stream
     * (type, initialization date, number of elements, etc).
     *
     * @param args arguments for the command.
     */
    @Override
    public void info(String[] args) throws IOException {
        // if (the command have easy parameters) than: print exception and return.
        if (args == null || args.length != 0) {
            err.println("info: this command hasn't parameters.");
            return;
        }

        // else print to ru.itmo.s284719.client's console an information about the collection.
        try {
            checkConnection();
            ObjectSocketChannel.sendObject(channel, new UserCommand(new Info(), login, password));
            Object object = ObjectSocketChannel.getObject(channel);

            if (!object.getClass().equals(String.class)) {
                err.println("Invalid object type returned: expected \"String\".");
            } else {
                out.println(object);
            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    /**
     * Output to standard output stream all
     * (the elements of the collection in the string representation).
     *
     * @param args the command's arguments.
     */
    @Override
    public void show(String[] args) throws IOException {
        // if (the command has easy parameters) than: print exception and return.
        if (args == null || args.length != 0) {
            err.println("show: this command hasn't parameters.");
            return;
        }

        // else: print that.
        try {
            checkConnection();
            ObjectSocketChannel.sendObject(channel, new UserCommand(new Show(), login, password));
            Object object = ObjectSocketChannel.getObject(channel);

            if (!object.getClass().equals(String.class)) {
                err.println("Invalid object type returned: expected \"String\".");
                return;
            }

            out.println(object);
        } catch (ClassNotFoundException e) {
            err.println(Arrays.toString(e.getStackTrace()));
        }
    }

    /**
     * Add a new element to the collection.
     *
     * @param args the command's arguments.
     */
    @Override
    public void add(String[] args) throws IOException {
        // if (the command has easy parameters) than: throw exception and return.
        if (args == null || args.length != 0) {
            err.println("add: this command has one composite parameter {element}.");
            return;
        }
        // else: read composite parameter (SpaceMarine) and add that to the collection.
        try {
            checkConnection();
            SpaceMarine spaceMarine = inputSpaceMarine();

            checkConnection();
            ObjectSocketChannel.sendObject(channel, new UserCommand(new Add(spaceMarine), login, password));
            Object object = ObjectSocketChannel.getObject(channel);


            if (object != null) {
                err.println("Invalid object type returned: expected \"null\".");
            }
        } catch (ClassNotFoundException e) {
            err.println(Arrays.toString(e.getStackTrace()));
        }
    }

    /**
     * Update the value of a collection element whose ID is equal to the specified one.
     *
     * @param args the command's arguments.
     */
    @Override
    public void update(String[] args) throws IOException {
        // declare element's ID.
        int id = 0;

        // if (the command hasn't only one easy parameter) than: print exception and return.
        if (args == null || args.length != 1) {
            err.println("update: this command has one easy \"ID\" and one composite {element} parameters.");
            return;
        }

        // else: check easy parameter (Integer, not null, greater than zero) and read composite parameter (SpaceMarine).
        try {
            id = Integer.parseInt(args[0]);
            if (id <= 0) {
                throw new IllegalArgumentException();
            }
        } catch (IllegalArgumentException | NullPointerException e) {
            // if (check is fail) than: print exception and return.
            err.println("update: this command has one easy \"ID\" and one composite {element} parameters.");
            return;
        }

        try {
            // read composite parameter (SpaceMarine).
            checkConnection();
            SpaceMarine spaceMarine = inputSpaceMarine();
            // update element from collection with the ID.
            checkConnection();
            ObjectSocketChannel.sendObject(channel, new UserCommand(new Update(id, spaceMarine), login, password));
            Object object = ObjectSocketChannel.getObject(channel);

            if (object != null) {
                err.println("Invalid object type returned: expected \"null\".");
            }
        } catch (ClassNotFoundException e) {
            err.println(Arrays.toString(e.getStackTrace()));
        }
    }

    /**
     * Delete an item from the collection by its ID.
     *
     * @param args the command's arguments.
     */
    @Override
    public void removeById(String[] args) throws IOException {
        try {
            // if (the command hasn't only one easy parameter) than: print exception and return.
            if (args == null || args.length != 1) {
                err.println("remove_by_id: this command has one easy parameter \"ID\".");
                return;
            }

            // else: check parameter (Integer, not null, greater than zero)
            // and remove element from collection with the ID.
            int id = Integer.parseInt(args[0]);
            if (id <= 0) {
                throw new IllegalArgumentException();
            }

            checkConnection();
            ObjectSocketChannel.sendObject(channel, new UserCommand(new RemoveById(id), login, password));
            Object object = ObjectSocketChannel.getObject(channel);

            if (object != null) {
                err.println("Invalid object type returned: expected \"null\".");
            }
        } catch (ClassNotFoundException e) {
            err.println(Arrays.toString(e.getStackTrace()));
        } catch (IllegalArgumentException e) {
            // if (check is fail) than: print exception and return.
            err.println("remove_by_id: the command's parameter is a positive number.");
        }
    }

    /**
     * Clear the collection.
     *
     * @param args the command's arguments.
     */
    @Override
    public void clear(String[] args) throws IOException {
        try {
            checkConnection();
            ObjectSocketChannel.sendObject(channel, new UserCommand(new Clear(), login, password));
            Object object = ObjectSocketChannel.getObject(channel);

            if (object != null) {
                err.println("Invalid object type returned: expected \"null\".");
            }
        } catch (ClassNotFoundException e) {
            err.println(Arrays.toString(e.getStackTrace()));
        }
    }

    /**
     * Save the collection to a file.
     *
     * @param args the command's arguments.
     */
    @Override
    public void save(String[] args) {
        err.println("save: this command is not available on the ru.itmo.s284719.client application.");
    }

    /**
     * Terminate the program without saving in to a fail.
     *
     * @param args the command's arguments.
     */
    @Override
    public void exit(String[] args) throws IOException {
        if (args == null || args.length != 0) {
            err.println("exit: this command hasn't parameters.");
            return;
        }

        try {
            channel.close();
        } finally {
            System.exit(0);
        }
    }

    /**
     * Output the first item in the collection and deletes it.
     *
     * @param args the command's arguments.
     */
    @Override
    public void removeHead(String[] args) throws IOException {
        // if (the command has parameters) than: print exception and return.
        if (args == null || args.length != 0) {
            err.println("remove_head: this command hasn't parameters.");
            return;
        }

        // else if (the queue isn't empty): print and remove element from head of the collection.
        try {
            checkConnection();
            ObjectSocketChannel.sendObject(channel, new UserCommand(new RemoveHead(), login, password));
            Object object = ObjectSocketChannel.getObject(channel);

            if (object == null) {
            } else if (object.getClass().equals(Pair.class)) {
                out.println(object);
            } else {
                err.println("Invalid object type returned: expected \"Optional<Pair<SpaceMarine, String>>\".");
            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    /**
     * Add a new item to the collection if its value is less than the smallest item in this collection.
     *
     * @param args the command's arguments.
     */
    @Override
    public void addIfMin(String[] args) throws IOException {
        // if (the command has easy parameters) than: print exception and return.
        if (args == null || args.length != 0) {
            err.println("add_if_min: this command has one composite parameter {element}.");
            return;
        }

        try {
            // else: read composite parameter (SpaceMarine).
            checkConnection();
            SpaceMarine spaceMarine = inputSpaceMarine();
            // if (the element is minimal) than: add the element to the collection.
            checkConnection();
            ObjectSocketChannel.sendObject(channel, new UserCommand(new AddIfMin(spaceMarine), login, password));
            Object object = ObjectSocketChannel.getObject(channel);

            if (object != null) {
                err.println("Invalid object type returned: expected \"null\".");
            }
        } catch (ClassNotFoundException e) {
            err.println(Arrays.toString(e.getStackTrace()));
        }
    }

    /**
     * Remove all items from the collection that exceed the specified value.
     *
     * @param args the command's arguments.
     */
    @Override
    public void removeGreater(String[] args) throws IOException {
        // if (the command has easy parameters) than: print exception and return.
        if (args == null || args.length != 0) {
            err.println("remove_greater: this command has one composite parameter {element}.");
            return;
        }

        try {
            // read composite parameter (SpaceMarine).
            checkConnection();
            SpaceMarine spaceMarine = inputSpaceMarine();
            // remove all elements from the collection greater than the element.
            checkConnection();
            ObjectSocketChannel.sendObject(channel, new UserCommand(new RemoveGreater(spaceMarine), login, password));
            Object object = ObjectSocketChannel.getObject(channel);

            if (object != null) {
                err.println("Invalid object type returned: expected \"null\".");
            }
        } catch (ClassNotFoundException e) {
            err.println(Arrays.toString(e.getStackTrace()));
        }
    }

    /**
     * Delete a single item from the collection,
     * the value of the field whose height is equivalent to the specified one.
     *
     * @param args the command's arguments.
     */
    @Override
    public void removeAnyByHeight(String[] args) throws IOException {
        // if (the command's parameters greater than one) than: print exception and return.
        if (args == null || args.length > 1) {
            err.println("remove_aby_by_height: this command has one easy \"Height\" or hasn't parameter.");
            return;
        }

        // else if (the command hasn't parameters) than: the command has one easy parameter "zero".
        if (args.length == 0) {
            checkConnection();
            ObjectSocketChannel.sendObject(channel,new UserCommand(new RemoveAnyByHeight(0), login, password));
            return;
        }

        // check the parameter (Integer)
        // and remove any element from the collection whose has a height equal to the height.
        try {
            int height = Integer.parseInt(args[0]);

            checkConnection();
            ObjectSocketChannel.sendObject(channel, new UserCommand(new RemoveAnyByHeight(height), login, password));
            Object object = ObjectSocketChannel.getObject(channel);

            if (object != null) {
                err.println("Invalid object type returned: expected \"null\".");
            }
        } catch (ClassNotFoundException e) {
            err.println(Arrays.toString(e.getStackTrace()));
        } catch (NumberFormatException e) {
            // if (check is fail) than: print the exception and return.
            err.println("remove_any_by_height: the command's parameter is a number.");
        }
    }

    /**
     * Output the average value of the height field for all items in the collection.
     *
     * @param args the command's arguments.
     */
    @Override
    public void averageOfHeight(String[] args) throws IOException {
        // if (the command has parameters) than: print the exception and return.
        if (args == null || args.length != 0) {
            err.println("average_of_height: this command hasn't parameters.");
            return;
        }

        // else: get and print ru.itmo.s284719.server's answer.
        try {
            checkConnection();
            ObjectSocketChannel.sendObject(channel, new UserCommand(new AverageOfHeight(), login, password));
            Object object = ObjectSocketChannel.getObject(channel);

            if (!object.getClass().equals(String.class)) {
                err.println("Invalid object type returned: expected \"String\".");
                return;
            }

            String str = (String) object;
            out.println(str);
        } catch (ClassNotFoundException e) {
            err.println(Arrays.toString(e.getStackTrace()));
        }
    }

    /**
     * Output the number of elements
     * whose melee Weapon field value is greater than the specified one.
     *
     * @param args the command's arguments.
     */
    public void countGreaterThanMeleeWeapon(String[] args) throws IOException {
        // if (the command hasn't only one easy parameter) than: print the exception and return.
        if (args == null || args.length != 1) {
            err.println("count_greater_than_melee_weapon: this command has one easy parameter \"MeleeWeapon\".");
            return;
        }

        // else: check parameter(MeleeWeapon) and print count elements from the collection
        // with MeleeWeapon greater than the MeleeWeapon.
        try {
            MeleeWeapon meleeWeapon = MeleeWeapon.valueOf(args[0]);
            // if (check is fail) than: print the exception and return.
            if (meleeWeapon == null) {
                err.println("count_greater_than_melee_weapon: this command has one easy parameter \"MeleeWeapon\" type of enumeration.");
                return;
            }

            // else: print the count.
            checkConnection();
            ObjectSocketChannel.sendObject(channel, new UserCommand(new CountGreaterThanMeleeWeapon(meleeWeapon), login, password));
            Object object = ObjectSocketChannel.getObject(channel);

            if (!object.getClass().equals(String.class)) {
                err.println("Invalid object type returned: expected \"String\".");
                return;
            }

            out.println(object);
        } catch (ClassNotFoundException e) {
            err.println(Arrays.toString(e.getStackTrace()));
        } catch (IllegalArgumentException e) {
            // warning: the code should not start.
            err.println("count_greater_than_melee_weapon: this command has one easy parameter \"MeleeWeapon\" type of enumeration.");
        }
    }
}