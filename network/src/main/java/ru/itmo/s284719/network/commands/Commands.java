package ru.itmo.s284719.network.commands;

import ru.itmo.s284719.network.Command;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Interface for ru.itmo.s284719.network.commands.commands*/
public interface Commands {
    @ru.itmo.s284719.network.Command(name = "help", desc = "справка по доступным коммандам")
    public default void help(String[] args) {
        if (args == null || args.length != 0) {
            System.out.println("help: у данной комманды нет параметров");
            return;
        }
        List<String> list = new ArrayList<>();
        for (Method method : Commands.class.getDeclaredMethods()) {
            StringBuilder strB = new StringBuilder();
            if (method.isAnnotationPresent(ru.itmo.s284719.network.Command.class)) {
                ru.itmo.s284719.network.Command cmd = method.getAnnotation(ru.itmo.s284719.network.Command.class);
                strB.append(cmd.name());
                if (!cmd.args().equals("")) {
                    strB.append(" " + cmd.args());
                }
                strB.append(" : " + cmd.desc() + "\n");
            }
            list.add(strB.toString());
        }
        Collections.sort(list);
        StringBuilder result = new StringBuilder("Список комманд:\n");
        for (String str : list) {
            result.append(str);
        }
        result.deleteCharAt(result.length() - 1);
        System.out.println(result.toString());
    }

    @ru.itmo.s284719.network.Command(name = "info", desc = "вывести в стандартный поток вывода информацию " +
            "о коллекции (тип, дата инициализации, количество элементов и т.д.)")
    public void info(String[] args) throws IOException, InterruptedException;

    @ru.itmo.s284719.network.Command(name = "show",
            desc = "вывести в стандартный поток вывода все " +
            "элементы коллекции в строковом представлении")
    public void show(String[] args) throws IOException, InterruptedException;

    @ru.itmo.s284719.network.Command(name = "add", numbArgs = 1, args = "{element}",
            desc = "добавить новый элемент в коллекцию")
    public void add(String[] args) throws IOException, SQLException, ClassNotFoundException;

    @ru.itmo.s284719.network.Command(name = "update", numbArgs = 2, args = "id, {element}",
            desc = "обновить значение элемента коллекции, id которого равен заданному")
    public void update(String[] args) throws IOException, SQLException, ClassNotFoundException;

    @ru.itmo.s284719.network.Command(name = "remove_by_id", numbArgs = 1, args = "id",
            desc = "удалить элемент из коллекции по его id")
    public void removeById(String[] args) throws IOException, SQLException, ClassNotFoundException;

    @ru.itmo.s284719.network.Command(name = "clear", desc = "очистить коллекцию")
    public void clear(String[] args) throws IOException, SQLException, ClassNotFoundException;

    @ru.itmo.s284719.network.Command(name = "save", desc = "сохранить коллекцию в файл")
    public void save(String[] args) throws IOException;

    @ru.itmo.s284719.network.Command(name = "execute_script", numbArgs = 1, args = "file_name",
            desc = "считать и исполнить скрипт из указанного файла. " +
            "В скрипте содержатся команды в таком же виде, " +
            "в котором их вводит пользователь в интерактивном режиме.")
    public void executeScript(String[] args) throws IOException, InvocationTargetException, IllegalAccessException;
    @ru.itmo.s284719.network.Command(name = "exit", desc = "завершить программу " +
            "(без сохранения в файл)")
    /** Метод, завершающий программу, без сахранения в файл */
    public default void exit(String[] args) throws IOException {
        // справка по данной комманде
        if (args != null && args.length == 0) {
            System.exit(0);
        }
    }

    @ru.itmo.s284719.network.Command(name = "remove_head",
            desc = "вывести первый элемент коллекции и удалить его")
    public void removeHead(String[] args) throws IOException, InterruptedException, SQLException, ClassNotFoundException;

    @ru.itmo.s284719.network.Command(name = "add_if_min", numbArgs = 1, args = "{element}",
            desc = "добавить новый элемент в коллекцию, если его " +
                    "значение меньше, чем у наименьшего " +
                    "элемента этой коллекции")
    public void addIfMin(String[] args) throws IOException, SQLException;

    @ru.itmo.s284719.network.Command(name = "remove_greater", numbArgs = 1, args = "{element}",
            desc = "удалить из коллекции все элементы, " +
                    "превышающие заданный")
    public void removeGreater(String[] args) throws IOException, SQLException;

    @ru.itmo.s284719.network.Command(name = "remove_any_by_height", numbArgs = 1, args = "height",
            desc = "удалить из коллекции один элемент, значение поля " +
                    "height которого эквивалентно заданному")
    public void removeAnyByHeight(String[] args) throws IOException, SQLException;

    @ru.itmo.s284719.network.Command(name = "average_of_height",
            desc = "вывести среднее значение поля height " +
                    "для всех элементов коллекции")
    public void averageOfHeight(String[] args) throws IOException, InterruptedException;

    @Command(name = "count_greater_than_melee_weapon", numbArgs = 1,
    args = "meleeWeapon", desc = "вывести количество элементов, " +
            "значение поля meleeWeapon которых больше заданного")
    public void countGreaterThanMeleeWeapon(String[] args) throws IOException, InterruptedException;
}
