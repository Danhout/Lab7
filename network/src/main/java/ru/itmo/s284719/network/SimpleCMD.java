package ru.itmo.s284719.network;

import ru.itmo.s284719.network.commands.Commands;
import ru.itmo.s284719.network.parser.Parser;
import ru.itmo.s284719.network.space.*;

import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.charset.Charset;
import java.util.*;

/**
 * Class for initialisation map of commands and running they.
 *
 * @version 0.4
 * @author Danhout.
 */
public abstract class SimpleCMD implements Commands {
    /**
     * The map for commands.
     */
    protected final Map<String, Method> mapCommands;
    /**
     * The stack for buffered input's streams.
     */
    public Stack<BufferedReader> stackReaders = new Stack<>();
    /**
     * The set for saving file's names whose run for execution scripts.
     */
    protected Set<String> set = new HashSet<>();
    /**
     * The buffered system's stream of input.
     */
    protected BufferedReader in = new BufferedReader(
            new InputStreamReader(
                    System.in, Charset.forName("UTF-8")));
    /**
     * The buffered system's stream of output with auto-flush.
     */
    protected PrintWriter out = new PrintWriter(
            new OutputStreamWriter(
                    System.out, Charset.forName("UTF-8")), true);
    /**
     * The buffered system's stream of error with auto-flush.
     */
    protected PrintWriter err = new PrintWriter(
            new OutputStreamWriter(
                    System.err, Charset.forName("UTF-8")), true);

    /** Constructor without parameters*/
    public SimpleCMD() {
        mapCommands = new HashMap<>();

        for (Method method : Commands.class.getDeclaredMethods()) {
            if (method.isAnnotationPresent(Command.class)) {
                Command cmd = method.getAnnotation(Command.class);
                mapCommands.put(cmd.name(), method);
            }
        }
    }

    /**
     * Execute command of server's console.
     *
     * @param strLine the normalise line from server's console for running command.
     */
    public void runCommand(String strLine) throws IOException {
        try {
            // separation parameters and command from the line.
            String[] strs = strLine.split(" ");
            String command = strs[0];
            String[] args = Arrays.copyOfRange(strs, 1, strs.length);

            // check correction of the command.
            Method method = mapCommands.get(command);
            if (method == null) {
                // if (command isn't correction) than: print the exception.
                if (!command.equals("")) {
                    err.println("\"" + command + "\" isn't a command.");
                    err.println("help - a command that output the list with open commands.");
                }
                return;
            }
            // else: execute the command.
            ru.itmo.s284719.network.Command cmd = method.getAnnotation(ru.itmo.s284719.network.Command.class);
            method.invoke(this, (Object) args);
        } catch (ArrayIndexOutOfBoundsException e) {
            // warning: the code must not be started.
            err.println("This command wasn't detected.");
            err.println("help - a command that output the list with open commands.");
        } catch (IllegalAccessException | IllegalArgumentException e) {
            // warning: print about thees exceptions.
            err.println("Exception: " + e.getMessage());
            err.println("help - a command that output the list with open commands.");
        } catch (InvocationTargetException e) {
            throw new IOException(e);
        }
    }

    /**
     * Read and execute the script from the specified file.
     * The script contains commands in the same format,
     * in which they are entered by the user in interactive mode.
     *
     * @param args arguments for the command.
     */
    @Override
    public void executeScript(String[] args) throws IOException {
        // if (the command has parameters) than: print exception and return.
        if (args == null || args.length != 1) {
            err.println("execute_script: this command has one easy parameter \"file's name\".");
            return;
        }

        // file's name
        String fileName = Parser.normalise(args[0]);

        // check recursion.
        if (set.contains(fileName)) {
            err.println("Recursion execution is fixed.");
            return;
        }

        // open file for reading.
        File file;
        BufferedReader fileReader;
        try {
            file = new File(fileName);
            fileReader = new BufferedReader(new FileReader(file));
        } catch (FileNotFoundException e) {
            // if (file not found) than: print exception.
            err.println("File for reading not found.");
            return;
        }

        // push the input's stream to stack.
        stackReaders.push(in);
        // add the file's name to the set with same withes.
        set.add(fileName);
        // initialise the input's stream with file's input's stream of the file.
        in = fileReader;

        try {
            // declare line.
            String line;

            // read commands from the input's stream.
            do {
                // prompt to enter.
                out.print("$");
                out.flush();

                // if (the received end-symbol) than: get old input's stream.
                in.mark(1);
                if (in.read() == -1) {
                    break;
                }

                // else: read line from the stream and normalise this.
                in.reset();
                line = Parser.normalise(in.readLine());
                // if (the stream isn't console's stream of server) than: output the line to server's console.
                if (!stackReaders.isEmpty()) {
                    out.println(line);
                }
                // execute the command.
                runCommand(line);
            } while (true);
        } finally {
            // pop a stream from stack to the the stream.
            in = stackReaders.pop();
            // remove the file's name from the set.
            set.remove(args[0]);
        }
    }

    /**
     * Input <code>SpaceMarine</code>'s name with checking.
     *
     * @return the name of the space marine.
     */
    protected String inputName() throws IOException {
        while (true) {
            // input from the stream line, check the space marine's name and return this.
            try {
                out.println("Enter a name: ");
                String str = readConsoleLine();
                SpaceMarine.checkName(str);
                return str;
            } catch (IllegalArgumentException | NullPointerException e) {
                // if (received exception (not IO)) than: print the exception and run the function again.
                err.println(e.getMessage());
            }
        }
    }

    /**
     * Input a X coordinate with checking.
     *
     * @return the X coordinate.
     */
    protected long inputX() throws IOException {
        while (true) {
            // read line from console, check (Long, Coordinates.checkX(x)) and return that.
            try {
                out.println("Enter a X: ");
                String str = readConsoleLine();
                Long x = Long.parseLong(str);
                Coordinates.checkX(x);
                return x;
            } catch (NumberFormatException e) {
                // if (The isn't number) than: print the exception and run the function again.
                err.println("Invalid input data format, re-enter.");
            } catch (IllegalArgumentException e) {
                // else if (received other exception (not IO)) than: print that and run the function again.
                err.println(e.getMessage());
            }
        }
    }

    /**
     * Input a Y coordinate with checking.
     *
     * @return the Y coordinate.
     */
    protected long inputY() throws IOException {
        while (true) {
            try {
                // read line from the stream, check the line (Long, Coordinates.checkY(y)) and return that.
                out.println("Enter a Y: ");
                String str = readConsoleLine();
                Long y = Long.parseLong(str);
                Coordinates.checkY(y);
                return y;
            } catch (NumberFormatException e) {
                // if (the isn't number) than: print about that and run the function again.
                err.println("Invalid input data format, re-enter.");
            } catch (NullPointerException e) {
                // else if (received other exception (not IO)) than: print that and run the function again.
                err.println(e.getMessage());
            }
        }
    }

    /**
     * Input coordinates with checking.
     *
     * @return coordinates.
     */
    protected Coordinates inputCoordinates() throws IOException {
        while (true) {
            try {
                // run functions inputX() and inputY() with checking and return that.
                out.println("Enter coordinates: ");
                Coordinates coordinates = new Coordinates();
                coordinates.setX(inputX());
                coordinates.setY(inputY());
                return coordinates;
            } catch (IllegalArgumentException | NullPointerException e) {
                // if (received exception (not IO)) than: print that and run the function again.
                err.println(e.getMessage());
            }
        }
    }

    /**
     * Input count of health with checking.
     *
     * @return the count of health.
     */
    protected long inputHealth() throws IOException {
        while (true) {
            try {
                // read line from the stream, check (Long, SpaceMarine.checkHealth(health)) and return that.
                out.println("Enter the amount of health: ");
                String str = readConsoleLine();
                Long health = Long.parseLong(str);
                SpaceMarine.checkHealth(health);
                return health;
            } catch (NumberFormatException e) {
                // if (the isn't number) than: print about that and run the function again.
                err.println("Invalid input data format, re-enter.");
            } catch (IllegalArgumentException | NullPointerException e) {
                // else if (received other exception (not IO)) than: print that and run the function again.
                err.println(e.getMessage());
            }
        }
    }

    /**
     * Input a height with checking.
     *
     * @return the height.
     */
    protected int inputHeight() throws IOException {
        while (true) {
            // read line from the stream, check (Integer, SpaceMarine.checkHeight(height)) and return that.
            try {
                out.println("Enter a height: ");
                String str = readConsoleLine();
                Integer height = Integer.parseInt(str);
                SpaceMarine.checkHeight(height);
                return height;
            } catch (NumberFormatException e) {
                // if (the isn't number) than: print about that and run the function again.
                err.println("Invalid input data format, re-enter.");
            }
        }
    }

    /**
     * Input astarte's category with checking.
     *
     * @return the astarte's category.
     */
    protected AstartesCategory inputCategory() throws IOException {
        while (true) {
            try {
                // print all categories.
                StringBuilder strB = new StringBuilder("Selected category:\r\n");
                for (AstartesCategory category : AstartesCategory.class.getEnumConstants()) {
                    strB.append(category + " ");
                }
                strB.setCharAt(strB.length() - 1, '.');
                out.println(strB.toString());
                // read line, check (AstartesCategory.check(category)) and return category.
                String str = readConsoleLine();
                AstartesCategory category = AstartesCategory.valueOf(str);
                AstartesCategory.check(category);
                return category;
            } catch (NullPointerException | IllegalArgumentException e) {
                // if (the isn't category) than: print exception about that and re-run the function.
                err.println("Invalid input data format, re-enter.");
            }
        }
    }

    /**
     * Input melee weapon with checking.
     *
     * @return the melee weapon.
     */
    protected MeleeWeapon inputMeleeWeapon() throws IOException {
        while (true) {
            try {
                // print all melee weapons.
                StringBuilder strB = new StringBuilder("Choose a melee weapon:\r\n");
                for (MeleeWeapon meleeWeapon : MeleeWeapon.values()) {
                    strB.append(meleeWeapon + " ");
                }
                strB.setCharAt(strB.length() - 1, '.');
                out.println(strB.toString());

                // read the melee weapon with checking.
                String str = readConsoleLine();
                MeleeWeapon weapon = MeleeWeapon.valueOf(str);
                MeleeWeapon.check(weapon);
                return weapon;
            } catch (IllegalArgumentException | NullPointerException e) {
                // if (the isn't melee weapon) than: print about that and re-run the function.
                err.println("Invalid input data format, re-enter.");
            }
        }
    }

    /**
     * Input a name of the Chapter with checking.
     *
     * @return the name of the chapter.
     */
    protected String inputNameOfChapter() throws IOException {
        while (true) {
            try {
                out.println("Enter the Chapter name: ");
                String str = readConsoleLine();
                Chapter.checkName(str);
                return str;
            } catch (IllegalArgumentException | NullPointerException e) {
                err.println(e.getMessage());
            }
        }
    }

    /**
     * Input a name of the legion with checking.
     *
     * @return the name of the legion.
     */
    protected String inputParentLegion() throws IOException {
        do {
            out.println("Enter the name of the Legion: ");
            String str = readConsoleLine();
            Chapter.checkParentLegion(str);
            return str;
        } while (true);
    }

    /**
     * Input a count of marines.
     *
     * @return the marines' count.
     */
    protected int inputMarinesCount() throws IOException {
        while (true) {
            try {
                out.println("Enter the count of Marines: ");
                String str = readConsoleLine();
                Integer count = Integer.parseInt(str);
                Chapter.checkMarinesCount(count);
                return count;
            } catch (NumberFormatException e) {
                err.println("Invalid input data format, re-enter.");
            } catch (IllegalArgumentException e) {
                err.println(e.getMessage());
            }
        }
    }

    /**
     * Input a name of the world.
     *
     * @return the world's name.
     */
    protected String inputWorld() throws IOException {
        while (true) {
            out.println("Enter the name of the world: ");
            String str = readConsoleLine();
            Chapter.checkWorld(str);
            return str;
        }
    }

    /**
     * Input a chapter with checking.
     *
     * @return the chapter.
     */
    protected Chapter inputChapter() throws IOException {
        while (true) {
            Chapter chapter = new Chapter();
            out.println("Enter a Chapter: ");
            chapter.setName(inputNameOfChapter());
            chapter.setParentLegion(inputParentLegion());
            chapter.setMarinesCount(inputMarinesCount());
            chapter.setWorld(inputWorld());
            return chapter;
        }
    }

    /**
     * Input a space marine.
     *
     * @return the space marine.
     */
    protected SpaceMarine inputSpaceMarine() throws IOException {
        while (true) {
            SpaceMarine spaceMarine = new SpaceMarine();
            spaceMarine.setName(inputName());
            spaceMarine.setCoordinates(inputCoordinates());
            spaceMarine.setHealth(inputHealth());
            spaceMarine.setHeight(inputHeight());
            spaceMarine.setCategory(inputCategory());
            spaceMarine.setMeleeWeapon(inputMeleeWeapon());
            spaceMarine.setChapter(inputChapter());
            return spaceMarine;
        }
    }

    /**
     * Read line from the input's stream and
     * if the line isn't from the server's console than print the normal line to server's console.
     *
     * @return the string after normalization.
     */
     protected String readConsoleLine() throws IOException {
        // print about successful input from the console.
        out.print("$");
        out.flush();

        // if (the received end-symbol) than: execute command exit.
        // out symbol to prompt you to enter.
        in.mark(1);
        if (in.read() == -1) {
            err.println("Received the program end symbol.");
            runCommand("exit");
        }

        // else: read line from server's console.
        in.reset();
        // normalization the line.
        String str = Parser.normalise(in.readLine());

        // if (script's execute) than: print the normal commands to server's console.
        if (!stackReaders.isEmpty()) {
            out.println(str);
        }
        return str;
    }
}
