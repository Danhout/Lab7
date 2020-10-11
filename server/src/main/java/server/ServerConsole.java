package server;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import database.DatabaseHandler;
import database.User;
import network.*;
import network.commands.*;
import network.commands.Command;
import network.commands.UserCommand;
import network.parser.Pair;
import network.space.MeleeWeapon;
import network.space.SpaceMarine;

import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.RecursiveAction;
import java.util.stream.Collectors;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

/**
 * Server's class for commands.
 *
 * @version 0.5
 * @author Danhout
 */
public class ServerConsole extends SimpleCMD {

    /* Log4j 2 */
    static final Logger logger = LogManager.getLogger(ServerConsole.class);

    /**
     * The queue for processing space marines and saving they.
     */
    private final Queue<Pair<SpaceMarine, String>> queuePair;
    /**
     * The time of creation the collection in milliseconds.
     */
    private final long creationTime;
    /**
     * The universal parser from GSON for format JSON.
     */
    private final Gson gson = Converters.registerZoneId(new GsonBuilder()).setPrettyPrinting().create();
    /**
     * The computer's local IP.
     */
    private String IP = "localhost";
    /**
     * The server's working port.
     * It's default value of 8000.
     */
    private int PORT = Server.DEFAULT_PORT;
    /**
     * The server's channel.
     */
    private ServerSocketChannel serverChannel;
    /**
     * The selector for registering server's channel for operation accept.
     */
    private Selector serverSelector;
    /**
     * The selector for registering client's channel for operation read.
     */
    private Selector clientSelector;
    /**
     * The list with client's channels.
     */
    private List<SocketChannel> listChannels = new LinkedList<>();
    /**
     * The thread for processing connections.
     */
    private Thread threadConnections;
    /**
     * The field with information about working server.
     */
    private boolean wasWorking = false;
    /**
     * The database's handler.
     */
    private DatabaseHandler dbHandler = null;
    /**
     * The admin's info with.
     */
    private User admin = null;
    /**
     * The pool for reading clients' requests;
     */
    private ForkJoinPool executor = new ForkJoinPool(4);

    /**
     * Constructor with all parameters.
     *
     * @param port the server's port.
     * @param queuePair the queue with SpaceMarines and creator's login for the server.
     */
    public ServerConsole(int port, PriorityBlockingQueue<Pair<SpaceMarine, String>> queuePair,
                         DatabaseHandler dbHandler, User admin) throws NoSuchAlgorithmException, IOException {

        // create ServerConsole with the SpaceMarine's queue and default functions.
        super();
        this.queuePair = queuePair;
        this.creationTime = System.currentTimeMillis();
        this.dbHandler = dbHandler;
        this.admin = admin;

        try {
            // initialization the IP and the port.
            IP = InetAddress.getByName("localhost").getHostAddress();
            PORT = port;

            // open server's and client's selectors.
            serverSelector = Selector.open();
            clientSelector = Selector.open();

            // open server's channel.
            serverChannel = ServerSocketChannel.open();
            // initialization not-blocking server's channel and register the channel to the server's selector
            // for operation accept of server's channel.
            serverChannel.bind(new InetSocketAddress(PORT)).configureBlocking(false).register(serverSelector, SelectionKey.OP_ACCEPT);

            // print IP and port about the created server.
            out.println("Server with IP: " + IP + ", Port: " + PORT + " is working...");

            // was print about working the server.
            wasWorking = true;

            // task for adding new connections.
            Runnable acceptConnectionsTask = () -> {
                Iterator<SelectionKey> keysServer = serverSelector.selectedKeys().iterator();
                try {
                    // received server-client channel and register that to clientSelector for operation read
                    SocketChannel channel = ((ServerSocketChannel) keysServer.next().channel()).accept();
                    channel.configureBlocking(false).register(clientSelector, SelectionKey.OP_READ);
                    // add the channel to listChannels with client's channels.
                    listChannels.add(channel);
                    // logging.
                    logger.info("Client with " +
                            "IP: " + channel.socket().getInetAddress().getHostAddress() +
                            ", PORT: " + channel.socket().getPort() + " " +
                            "is connected");
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    keysServer.remove();
                }
            };
            // task for removing connections.
            Runnable removeIncorrectConnectionsTask = () -> {
                // remove closed client's channels from client's selector and list with channels.
                Iterator<SocketChannel> channels = listChannels.iterator();
                while (channels.hasNext()) {
                    SocketChannel channel = channels.next();
                    if (!channel.isConnected()) {
                        clientSelector.keys().remove(channel.keyFor(clientSelector));
                        channels.remove();
                        // logging.
                         logger.info("Client with " +
                                 "IP: " + channel.socket().getInetAddress().getHostAddress() +
                                 ", PORT: " + channel.socket().getPort() +
                                 " is disconnected");
                    }
                }
            };

            // initialization new thread for processing of client-server connections.
            threadConnections = new Thread(() -> {
                do {
                    try {
                        // receive new connections.
                        if (serverSelector.selectNow() != 0) {
                            acceptConnectionsTask.run();
                        }
                        // process client's command.
                        if (clientSelector.selectNow() != 0) {
                            Set<SelectionKey> setKeys = clientSelector.selectedKeys();
                            executor.invoke(new RequestAction(setKeys.spliterator()));
                            setKeys.clear();
                        }
                        // remove incorrect connections.
                        if (!listChannels.isEmpty()) {
                            removeIncorrectConnectionsTask.run();
                        }
                    } catch (IOException e) {
                        // logging.
                        logger.error("Connections' process have a exception: " + e.getMessage());
                    }
                } while (true);
            });
            // start the thread for processing connections.
            threadConnections.start();

            // work server's console.
            do {
                try {
                    // execute command from the server's console.
                    runCommand(readConsoleLine());
                } catch (Exception e) {
                    // if (received exception) than: execute command exit.
                    runCommand("exit");
                }
            } while (true);
        } catch (IOException e) {
            // err.println(e.getMessage());
            if (wasWorking) { err.println("The server is closed."); }
        }
    }

    class RequestAction extends RecursiveAction {
        Spliterator<SelectionKey> keysRequests;

        public RequestAction(Spliterator<SelectionKey> keysRequests) {
            this.keysRequests = keysRequests;
        }

        @Override
        protected void compute() {
            // if (the keys is empty), than: exit the method;
            if (keysRequests == null || keysRequests.estimateSize() == 0)
                return;
            // else if (the keys have only one SelectionKey element), than:
            else if (keysRequests.estimateSize() == 1) {
                keysRequests.forEachRemaining(key -> {
                    // get a SocketChannel from the SelectionKey.
                    SocketChannel channel = (SocketChannel) key.channel();
                    // process client's command with the database.
                    new Thread(() -> {
                        try {
                            Object result = new ClientCallable(channel).call();
                            /*Object result = / objectSockeChannel.sendObject()*/
                            // send the result to client.
                            new Thread(() -> {
                                try {
                                    ObjectSocketChannel.sendObject(channel, result);
                                } catch (Exception e) {
                                    // logging.
                                    logger.error("Sending has the exception: " + e.getMessage());
                                }
                            }).start();
                        } catch (Exception e) {
                            // logging.
                             logger.error("Processing client's request has th exception" + e.getMessage());
                        }
                    }).start();
                });
            // else: split the spliterator and invoke updated the object and new object.
            } else {
                RequestAction newRequestAction = new RequestAction(keysRequests.trySplit());
                invokeAll(this, newRequestAction);
            }
        }
    }

    class ClientCallable implements Callable {
        UserCommand userCmd;
        User user;

        public ClientCallable(SocketChannel channel)
                throws Exception {
            this.userCmd = (UserCommand) ObjectSocketChannel.getObject(channel);
            if (userCmd.getPassword() != null)
                this.user = new User(userCmd.getLogin(), userCmd.getPassword());
        }

        @Override
        public Object call()
                throws IllegalAccessException, SQLException, ClassNotFoundException {
            // if (command is empty) than: authorize or register the client.
            if (userCmd.getCommand() == null)
                return checkUserData();
            // Invoke method with client command class's name.
            try {
                Command cmd = (Command) userCmd.getCommand();
                String cmdName = cmd.getClass().getSimpleName();
                // do to lower case first symbol the command's class.
                cmdName = Character.toLowerCase(cmdName.charAt(0)) + cmdName.substring(1);
                Method method = ClientCallable.class.getDeclaredMethod(cmdName);
                // run client's command
                return method.invoke(this);
            } catch (InvocationTargetException e) {
                // for logging.
                logger.error("Client's command have a exception: " + e.getMessage());
            } catch (NoSuchMethodException e) {
                // logging.
                String cmdName = userCmd.getCommand().getClass().getSimpleName();
                cmdName = Character.toLowerCase(cmdName.charAt(0)) + cmdName.substring(1);
                logger.error("Client's command :\"" + cmdName + " isn't found");
            }
            return null;
        }

        Boolean checkUserData() throws SQLException, ClassNotFoundException {
            // if (password is empty) than: send Boolean.of(login is free).
            if (userCmd.getPassword() == null)
                return Boolean.valueOf(dbHandler.isLoginFree(userCmd.getLogin()));
            // if (full user's data and the login is free) than: register user.
            else if (dbHandler.isLoginFree(userCmd.getLogin())) {
                dbHandler.registerUser(user);
                return null;
                // if (full user's data but the login isn't free) than: login user.
            }
            return Boolean.valueOf(dbHandler.isRegisteredUser(user));
        }

        // add spaceMarine to queue.
        Object add() throws SQLException, ClassNotFoundException {
            SpaceMarine spaceMarine = ((Add) userCmd.getCommand()).spaceMarine;
            spaceMarine.setId(dbHandler.addSpaceMarineWithCreator(spaceMarine, user));
            queuePair.add(new Pair<>(spaceMarine, user.getLogin()));
            return null;
        }

        // if (the the spaceMarine's less than all spaceMarines from the queue) than: add that to queue
        Object addIfMin() throws SQLException, ClassNotFoundException {
            // get minimal spaceMarine from the queue.
            SpaceMarine spaceMarine = ((AddIfMin) userCmd.getCommand()).spaceMarine;
            Optional<Pair<SpaceMarine, String>> optional = queuePair.stream().min(Pair::compareToFirst);
            // if (queue isn't empty and new spaceMarine less than the old minimal spaceMarine)
            if (!optional.isPresent() || spaceMarine.compareTo(optional.get().first) < 0) {
                // than: add new spaceMarine to the database and the queue.
                spaceMarine.setId(dbHandler.addSpaceMarineWithCreator(spaceMarine, user));
                queuePair.add(new Pair<>(spaceMarine, user.getLogin()));
            }
            return null;
        }

        // send average of height of spaceMarines from the queue to client.
        Object averageOfHeight() {
            String str;
            if (queuePair.isEmpty())
                str = "The average value of the height: 0.";
            else {
                int result = queuePair.stream().mapToInt(pair -> pair.first.getHeight()).sum();
                str = "The average value of the height: " + ((double) result) / queuePair.size() + ".";
            }
            return str;
        }

        // clear the queue.
        Object clear() throws SQLException, ClassNotFoundException {
            Iterator<Pair<SpaceMarine, String>> iter = queuePair.iterator();
            while (iter.hasNext()) {
                Pair<SpaceMarine, String> pair = iter.next();
                if (pair.second.equals(user.getLogin())) {
                    dbHandler.deleteSpaceMarineWithCreator(pair.first.getId());
                    iter.remove();
                }
            }
            return null;
        }

        // send count spaceMarines from the queue greater than the meleeWeapon.
        Object countGreaterThanMeleeWeapon() {
            MeleeWeapon meleeWeapon = ((CountGreaterThanMeleeWeapon) userCmd.getCommand()).meleeWeapon;
            long count = queuePair.stream()
                    .filter(pair -> pair.first.getMeleeWeapon().compareTo(meleeWeapon) > 0)
                    .count();
            return count + " queue's elements have the value \"Melee Weapon\", greater than the given value.";
        }

        // send info about the queue to client.
        Object info() {
            return "collectionType: PriorityQueue<SpaceMarine>, " +
                    "createTime: " + new SimpleDateFormat("hh:mm:ss dd-MM-yyyy").format(creationTime) +
                    ", length: " + queuePair.size() + ".";
        }

        // remove any spaceMarine from the queue with height less than the height.
        Object removeAnyByHeight() throws SQLException, ClassNotFoundException {
            int height = ((RemoveAnyByHeight) userCmd.getCommand()).height;
            Iterator<Pair<SpaceMarine, String>> iter = queuePair.iterator();
            while (iter.hasNext()) {
                Pair<SpaceMarine, String> pair = iter.next();
                if (pair.second.equals(user.getLogin()) && pair.first.getHeight().equals(height)) {
                    dbHandler.deleteSpaceMarineWithCreator(pair.first.getId());
                    iter.remove();
                    break;
                }
            }
            return null;
        }

        // remove spaceMarine by the ID.
        Object removeById() throws SQLException, ClassNotFoundException {
            int id = ((RemoveById) userCmd.getCommand()).id;
            Iterator<Pair<SpaceMarine, String>> iter = queuePair.iterator();
            while (iter.hasNext()) {
                Pair<SpaceMarine, String> pair = iter.next();
                if (pair.first.getId() == id) {
                    if (pair.second.equals(user.getLogin())) {
                        dbHandler.deleteSpaceMarineWithCreator(pair.first.getId());
                        iter.remove();
                    }
                    break;
                }
            }
            return null;
        }

        // remove all spaceMarine from the queue greater than the spaceMarine.
        Object remove_greater() throws SQLException, ClassNotFoundException {
            SpaceMarine spaceMarine = ((RemoveGreater) userCmd.getCommand()).spaceMarine;
            Iterator<Pair<SpaceMarine, String>> iter = queuePair.iterator();
            while (iter.hasNext()) {
                Pair<SpaceMarine, String> pair = iter.next();
                if (pair.second.equals(user.getLogin()) && pair.first.compareTo(spaceMarine) > 0) {
                    dbHandler.deleteSpaceMarineWithCreator(pair.first.getId());
                    iter.remove();
                }
            }
            return null;
        }

        // send and remove spaceMarine from a head of the queue.
        Object removeHead() throws SQLException, ClassNotFoundException {
            if (queuePair.peek().second.equals(user.getLogin())) {
                dbHandler.deleteSpaceMarineWithCreator(queuePair.peek().first.getId());
                return queuePair.poll();
            }
            return null;
        }

        // send the queue in the format JSON to client.
        Object show() {
            List<Pair<SpaceMarine, String>> list = queuePair.stream()
                    .sorted(Comparator.comparing(pair -> pair.first.getHeight()))
                    .collect(Collectors.toList());
            return gson.toJson(list);
        }

        // update spaceMarine with same ID.
        Object update() throws SQLException, ClassNotFoundException {
            Update updateValue = (Update) userCmd.getCommand();
            int id = updateValue.id;
            SpaceMarine spaceMarine = updateValue.newSpaceMarine;
            Iterator<Pair<SpaceMarine, String>> iter = queuePair.iterator();
            while (iter.hasNext()) {
                Pair<SpaceMarine, String> pair = iter.next();
                if (pair.first.getId() == id) {
                    if (pair.second.equals(user.getLogin())) {
                        dbHandler.deleteSpaceMarineWithCreator(id);
                        iter.remove();
                        spaceMarine.setId(dbHandler.addSpaceMarineWithCreator(spaceMarine, user));
                        queuePair.add(new Pair<>(spaceMarine, user.getLogin()));
                    }
                    break;
                }
            }
            return null;
        }
    }

    /**
     * Output information about the collection to the standard output stream
     * (type, initialization date, number of elements, etc).
     *
     * @param args arguments for the command.
     */
    @Override
    public void info(String[] args) {
        // if (the command have easy parameters) than: print exception and return.
        if (args == null || args.length != 0) {
            err.println("info: this command hasn't parameters.");
            return;
        }

        String str = "collectionType: PriorityQueue<SpaceMarine>, " +
                "createTime: " + new SimpleDateFormat("hh:mm:ss dd-MM-yyyy").format(creationTime) +
                ", length: " + queuePair.size() + ".";
        out.println(str);
    }

    /**
     * Output to standard output stream all
     * (the elements of the collection in the string representation).
     *
     * @param args the command's arguments.
     */
    @Override
    public void show(String[] args) {
        // if (the command has easy parameters) than: print exception and return.
        if (args == null || args.length != 0) {
            err.println("show: this command hasn't parameters.");
            return;
        }

        List<Pair<SpaceMarine, String>> list = queuePair.stream()
                .sorted(Comparator.comparing(pair -> pair.first.getHeight()))
                .collect(Collectors.toList());
        out.println(gson.toJson(list));
    }

    /**
     * Add a new element to the collection.
     *
     * @param args the command's arguments.
     */
    @Override
    public void add(String[] args) throws IOException, SQLException, ClassNotFoundException {
        // if (the command has easy parameters) than: throw exception and return.
        if (args == null || args.length != 0) {
            err.println("add: this command has one composite parameter {element}.");
            return;
        }

        SpaceMarine spaceMarine = inputSpaceMarine();
        spaceMarine.setId(dbHandler.addSpaceMarineWithCreator(spaceMarine, admin));
        queuePair.add(new Pair<>(spaceMarine, admin.getLogin()));
    }

    /**
     * Update the value of a collection element whose ID is equal to the specified one.
     *
     * @param args the command's arguments.
     */
    @Override
    public void update(String[] args) throws IOException, SQLException, ClassNotFoundException {
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

        SpaceMarine spaceMarine = inputSpaceMarine();
        Iterator<Pair<SpaceMarine, String>> iter = queuePair.iterator();
        while (iter.hasNext()) {
            Pair<SpaceMarine, String> pair = iter.next();
            if (pair.first.getId() == id) {
                dbHandler.deleteSpaceMarineWithCreator(id);
                iter.remove();
                spaceMarine.setId(dbHandler.addSpaceMarineWithCreator(spaceMarine, admin));
                queuePair.add(new Pair<>(spaceMarine, admin.getLogin()));
                break;
            }
        }

    }

    /**
     * Delete an item from the collection by its ID.
     *
     * @param args the command's arguments.
     */
    @Override
    public void removeById(String[] args) throws SQLException, ClassNotFoundException {
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

            Iterator<Pair<SpaceMarine, String>> iter = queuePair.iterator();
            while (iter.hasNext()) {
                Pair<SpaceMarine, String> pair = iter.next();
                if (pair.first.getId() == id) {
                    dbHandler.deleteSpaceMarineWithCreator(id);
                    iter.remove();
                    break;
                }
            }

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
    public void clear(String[] args) throws SQLException, ClassNotFoundException {
        Iterator<Pair<SpaceMarine, String>> iter = queuePair.iterator();
        while (iter.hasNext()) {
            Pair<SpaceMarine, String> pair = iter.next();
            dbHandler.deleteSpaceMarineWithCreator(pair.first.getId());
            iter.remove();
        }
    }

    /**
     * Save the collection to a file.
     *
     * @param args the command's arguments.
     */
    @Override
    public void save(String[] args) {
       try {
           PrintWriter fout = new PrintWriter("base.json");
           fout.println(gson.toJson(queuePair));
           fout.flush();
           fout.close();
           out.println("Saving the collection to file \"base.json\" is corrected.");
       } catch (IOException e) {
           err.println("Saving the collection to file \"base.json\" is failed.");
           //for logging.
           //e.printStackTrace;
       }
    }

    /**
     * Terminate the program without saving in to a fail.
     *
     * @param args the command's arguments.
     */
    @Override
    public void exit(String[] args) {
        if (args == null || args.length != 0) {
            err.println("exit: this command hasn't parameters.");
            return;
        }

        // Exit program with trying to save collection to "base.json" file.
        try {
            save(null);
            out.println("Exit program.");
            out.flush();
            out.close();
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
    public void removeHead(String[] args) throws SQLException, ClassNotFoundException {
        // if (the command has parameters) than: print exception and return.
        if (args == null || args.length != 0) {
            err.println("remove_head: this command hasn't parameters.");
            return;
        }

        try {
            // else if (the queue isn't empty): print and remove element from head of the collection.
            if (queuePair.size() != 0) {
                Pair<SpaceMarine, String> pair = queuePair.poll();
                dbHandler.deleteSpaceMarineWithCreator(pair.first.getId());
                out.println(pair);
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * Add a new item to the collection if its value is less than the smallest item in this collection.
     *
     * @param args the command's arguments.
     */
    @Override
    public void addIfMin(String[] args) throws IOException, SQLException {
        // if (the command has easy parameters) than: print exception and return.
        if (args == null || args.length != 0) {
            err.println("add_if_min: this command has one composite parameter {element}.");
            return;
        }

        try {
            // get minimal spaceMarine from the queue.
            SpaceMarine spaceMarine = inputSpaceMarine();
            Optional<Pair<SpaceMarine, String>> optional = queuePair.stream().min(Pair::compareToFirst);
            // if (queue isn't empty and new spaceMarine less than the old minimal spaceMarine)
            if (!optional.isPresent() || spaceMarine.compareTo(optional.get().first) < 0) {
                // than: add new spaceMarine to the database and the queue.
                dbHandler.addSpaceMarineWithCreator(spaceMarine, admin);
                queuePair.add(new Pair<>(spaceMarine, admin.getLogin()));
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
    public void removeGreater(String[] args) throws IOException, SQLException {
        // if (the command has easy parameters) than: print exception and return.
        if (args == null || args.length != 0) {
            err.println("remove_greater: this command has one composite parameter {element}.");
            return;
        }

        try {
            // read composite parameter (SpaceMarine).
            SpaceMarine spaceMarine = inputSpaceMarine();
            Iterator<Pair<SpaceMarine, String>> iter = queuePair.iterator();
            while (iter.hasNext()) {
                Pair<SpaceMarine, String> pair = iter.next();
                if (pair.first.compareTo(spaceMarine) > 0) {
                    dbHandler.deleteSpaceMarineWithCreator(pair.first.getId());
                    iter.remove();
                }
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
    public void removeAnyByHeight(String[] args) throws SQLException {
        // if (the command's parameters greater than one) than: print exception and return.
        if (args == null || args.length > 1) {
            err.println("remove_aby_by_height: this command has one easy \"Height\" or hasn't parameter.");
            return;
        }

        // else if (the command hasn't parameters) than: the command has one easy parameter "zero".
        if (args.length == 0) {

            return;
        }

        // check the parameter (Integer)
        // and remove any element from the collection whose has a height equal to the height.
        try {
            int height;
            if (args.length == 0)
                height = 0;
            else
                height = Integer.parseInt(args[0]);

            Iterator<Pair<SpaceMarine, String>> iter = queuePair.iterator();
            while (iter.hasNext()) {
                Pair<SpaceMarine, String> pair = iter.next();
                if (pair.first.getHeight().equals(height)) {
                    dbHandler.deleteSpaceMarineWithCreator(pair.first.getId());
                    iter.remove();
                    break;
                }
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
    public void averageOfHeight(String[] args) {
        // if (the command has parameters) than: print the exception and return.
        if (args == null || args.length != 0) {
            err.println("average_of_height: this command hasn't parameters.");
            return;
        }

        // else: get and print server's answer.
        String str;
        if (queuePair.isEmpty()) {
            str = "The average value of the height: 0.";
        } else {
            int result = queuePair.stream().mapToInt(pair -> pair.first.getHeight()).sum();
            str = "The average value of the height: " + ((double) result) / queuePair.size() + ".";
        }
        out.println(str);
    }

    /**
     * Output the number of elements
     * whose melee Weapon field value is greater than the specified one.
     *
     * @param args the command's arguments.
     */
    public void countGreaterThanMeleeWeapon(String[] args) {
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
            long count = queuePair.stream()
                    .filter(pair -> pair.first.getMeleeWeapon().compareTo(meleeWeapon) > 0)
                    .count();
            out.println(count + " queue's elements have the value \"Melee Weapon\", greater than the given value.");

        } catch (IllegalArgumentException e) {
            // warning: the code should not start.
            err.println("count_greater_than_melee_weapon: this command has one easy parameter \"MeleeWeapon\" type of enumeration.");
        }
    }
}
