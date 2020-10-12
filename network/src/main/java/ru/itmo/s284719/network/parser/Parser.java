package ru.itmo.s284719.network.parser;

import ru.itmo.s284719.network.space.*;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.PriorityQueue;

/**
 * Parser for parse text in format JSON to collection in format priority queue.
 *
 * @version 0.1
 * @author Danhout.
 */
public class Parser {
    public PriorityQueue<SpaceMarine> jsonToPriorityQueue (String fileName)
            throws IllegalArgumentException, IOException {
        PriorityQueue<SpaceMarine> queue = new PriorityQueue<>();

        try (BufferedReader reader = new BufferedReader(new FileReader(fileName))) {

            Integer numberLine = 1;
            Integer numberChar = 0;

            Pair<Integer, Integer> pair = readCharacter('[', reader, numberLine, numberChar);
            numberLine = pair.first;
            numberChar = pair.second;

            char c = '\r';

            if (isFirstCharacter(']', reader)) {
                return queue;
            }

            do {
                Trio<SpaceMarine, Integer, Integer> trioSM = readSpaceMarine(reader, numberLine, numberChar);
                queue.add(trioSM.first);
                numberLine = trioSM.second;
                System.err.println("\r\n" + numberLine + "\r\n");
                numberChar = trioSM.third;

                if (isFirstCharacter(',', reader)) {
                    pair = readCharacter(',', reader, numberLine, numberChar);
                    numberLine = pair.first;
                    numberChar = pair.second;
                }

            } while (isFirstCharacter('{', reader));

            pair = readCharacter(']', reader, numberLine, numberChar);

            return queue;

        } catch (IllegalArgumentException | IOException e) { //IOException <- FileNotFoundException
            throw e;
        }
    }

    private boolean isFirstCharacter(char temp, BufferedReader reader)
            throws IOException
    {
        reader.mark(4096);
        char c = '\r';

        while (reader.ready() && (Character.isWhitespace(c) || Character.isSpaceChar(c))) {
            c = (char) reader.read();
        }
        reader.reset();

        if (c == temp) {
            return true;
        }

        return false;
    }

    private Pair<Integer, Integer> readCharacter(char temp,
                                                 BufferedReader reader,
                                                 Integer numberLine,
                                                 Integer numberChar)
            throws IllegalArgumentException, IOException
    {
        char c = '\r';

        while (reader.ready() && c != temp) {
            c = (char) reader.read();
            numberChar++;

            if (c == '\n') {
                numberChar = 0;
                numberLine++;

            } else if (!Character.isWhitespace(c) && !Character.isSpaceChar(c) && c != temp) {
                throw new IllegalArgumentException("Wrong: the character isn't '" + temp + "':\n" +
                        "Line's number: " + numberLine + "\n" +
                        "Char's number: " + numberChar + "\n" +
                        "Symbol: '" + c + "'");
            }

        }

        if (c != temp) {
            throw new IllegalArgumentException("Wrong: the file must have '" + temp + "' after the text");
        }

        return new Pair<Integer, Integer>(numberLine, numberChar);
    }

    private Trio<String, Integer, Integer> readWord(BufferedReader reader,
                                                    Integer numberLine,
                                                    Integer numberChar)
            throws IllegalArgumentException, IOException
    {
        char c = '\r';

        while(reader.ready() && c!= '"') {
            c = (char) reader.read();
            numberChar++;

            if (c == '\n') {
                numberChar = 0;
                numberLine++;
            } else if (c != '"' && !Character.isWhitespace(c) && !Character.isSpaceChar(c)) {
                throw new IllegalArgumentException("Wrong: the character isn't '\"':\n" +
                        "Line's number: " + numberLine + "\n" +
                        "Char's number: " + numberChar + "\n" +
                        "Symbol: '" + c + "'");
            }
        }

        if (c != '"') {
            throw new IllegalArgumentException("Wrong: the file must have \"...\" with word(s)");
        }

        c = '\r';
        StringBuilder sb = new StringBuilder();

        while (reader.ready() && c != '"') {
            c = (char) reader.read();
            numberChar++;

            if(c == '\n') {
                numberChar = 0;
                numberLine++;
            }

            sb.append(c);
        }

        if (c != '"') {
            throw new IllegalArgumentException("Wrong: the file must have '\"' after word(s)");
        }

        sb.deleteCharAt(sb.length() - 1);
        return new Trio<String, Integer, Integer>(sb.toString(), numberLine, numberChar);
    }

    public static String normalise(String string) {
         StringBuilder sb = new StringBuilder();
         boolean space = false;

         for (char c : string.toCharArray()) {
             if (Character.isSpaceChar(c) || Character.isWhitespace(c)) {
                 space = true;
             } else if (space && sb.length() != 0) {
                 sb.append(" " + c);
                 space = false;
             } else {
                 sb.append(c);
                 space = false;
             }
         }

         return sb.toString();
    }

    private Trio<Long, Integer, Integer> readLong(BufferedReader reader,
                                                  Integer numberLine,
                                                  Integer numberChar)
            throws IllegalArgumentException, IOException
    {
        char c = '\r';

        while (reader.ready() && !Character.isDigit(c) && c != '+' && c != '-') {

            if (!Character.isWhitespace(c) && !Character.isSpaceChar(c)) {
                reader.reset();
                numberChar--;
                return null;
            } else if (c == '\n') {
                numberLine++;
                numberChar = 0;
            }

            reader.mark(2);
            c = (char) reader.read();
            numberChar++;
        }

        long k = 1;

        if (c == '-' || c == '+') {

            if(c == '-') {
                k = -1;
            }

            c = '\r';

            while (reader.ready() && !Character.isDigit(c)) {

                if (!Character.isWhitespace(c) && !Character.isSpaceChar(c)) {
                    reader.reset();
                    numberChar--;
                    throw new IllegalArgumentException("\"Wrong: there must be null or long integer\n" +
                            "Line's number: " + numberLine + "\n" +
                            "Char's number: " + numberChar);
                } else if (c == '\n') {
                    numberLine++;
                    numberChar = 0;
                }

                reader.mark(1);
                c = (char) reader.read();
                numberChar++;
            }
        }

        Long l1 = 0L;

        while (reader.ready() && (Character.isDigit(c) || Character.isWhitespace(c) || Character.isSpaceChar(c))) {

            if (Character.isDigit(c)) {
                Long l2 = l1 * 10 - c + '0';

                if (l2 > l1) {
                    throw new IllegalArgumentException("Wrong: the long integer is very big\n" +
                            "Line's number: " + numberLine + "\n" +
                            "Char's number: " + numberChar);
                }

                l1 = l2;
            } else if (c == '\n') {
                numberLine++;
                numberChar = 0;
            }

            reader.mark(1);
            c = (char) reader.read();
            numberChar++;
        }

        reader.reset();
        numberChar--;

        if (k == 1 && l1.equals(Long.MIN_VALUE)) {
            throw new IllegalArgumentException("Wrong: the long integer is very big\n" +
                    "Line's number: " + numberLine + "\n" +
                    "Char's number: " + numberChar);
        }

        l1 *= -k;

        return new Trio<Long, Integer, Integer>(l1, numberLine, numberChar);
    }

    private Trio<Integer, Integer, Integer> readInteger(BufferedReader reader,
                                                  Integer numberLine,
                                                  Integer numberChar)
            throws IllegalArgumentException, IOException
    {
        Trio<Long, Integer, Integer> trioLong = readLong(reader, numberLine, numberChar);
        long l = trioLong.first;
        int i = (int) l;
        long li = i;

        if (l != li) {
            throw new IllegalArgumentException("Wrong: the integer is long\n" +
                    "Line's number: " + trioLong.second + "\n" +
                    "Char's number: " + trioLong.third);
        }

        return new Trio<>(i, trioLong.second, trioLong.third);
    }

    private Pair<Integer, Integer> readAttribut(String attr,
                                                BufferedReader reader,
                                                Integer numberLine,
                                                Integer numberChar)
            throws IllegalArgumentException, IOException
    {
        Trio<String, Integer, Integer> trio = readWord(reader, numberLine, numberChar);
        String word = normalise(trio.first);

        if (!word.equals(attr)) {
            throw new IllegalArgumentException("Wrong: the attribut's name isn't \"" + attr + "\"\n" +
                    "Lines' numbers: " + numberLine + "-" + trio.second + "\n" +
                    "Chars' numbers: " + numberChar + "-" + trio.third + "\n" +
                    "String: \"" + word + "\"");
        } 

        Pair<Integer, Integer> pair = readCharacter(':', reader, trio.second, trio.third);

        return new Pair<>(pair.first, pair.second);
    }

    private Trio<SpaceMarine, Integer, Integer> readSpaceMarine(BufferedReader reader,
                                                                Integer numberLine,
                                                                Integer numberChar)
            throws IllegalArgumentException, IOException
    {
        SpaceMarine spaceMarine = new SpaceMarine();

        Pair<Integer, Integer> pair = readCharacter('{', reader, numberLine, numberChar);
        numberLine = pair.first;
        numberChar = pair.second;

        // read SpaceMarine.name
        pair = readAttribut("name", reader, numberLine, numberChar);
        numberLine = pair.first;
        numberChar = pair.second;

        Trio<String, Integer, Integer> trioStr = readWord(reader, numberLine, numberChar);
        numberLine = trioStr.second;
        numberChar = trioStr.third;
        spaceMarine.setName(normalise(trioStr.first));

        pair = readCharacter(',', reader, numberLine, numberChar);
        numberLine = pair.first;
        numberChar = pair.second;

        Coordinates coordinates = new Coordinates();
        // read SpaceMarine.coordinates
        pair = readAttribut("coordinates", reader, numberLine, numberChar);
        numberLine = pair.first;
        numberChar = pair.second;

        pair = readCharacter('{', reader, numberLine, numberChar);
        numberLine = pair.first;
        numberChar = pair.second;

        // read SpaceMarine.coordinates.x
        pair = readAttribut("x", reader, numberLine, numberChar);
        numberLine = pair.first;
        numberChar = pair.second;

        Trio<Long, Integer, Integer> trioLong = readLong(reader, numberLine, numberChar);
        numberLine = trioLong.second;
        numberChar = trioLong.third;
        coordinates.setX(trioLong.first);

        pair = readCharacter(',', reader, numberLine, numberChar);
        numberLine = pair.first;
        numberChar = pair.second;

        // read SpaceMarine.coordinates.y
        pair = readAttribut("y", reader, numberLine, numberChar);
        numberLine = pair.first;
        numberChar = pair.second;

        trioLong = readLong(reader, numberLine, numberChar);
        coordinates.setY(trioLong.first);
        numberLine = trioLong.second;
        numberChar = trioLong.third;

        pair = readCharacter('}', reader, numberLine, numberChar);
        numberLine = pair.first;
        numberChar = pair.second;

        pair = readCharacter(',', reader, numberLine, numberChar);
        numberLine = pair.first;
        numberChar = pair.second;

        spaceMarine.setCoordinates(coordinates);

        // read SpaceMarine.health
        pair = readAttribut("health", reader, numberLine, numberChar);
        numberLine = pair.first;
        numberChar = pair.second;

        trioLong = readLong(reader, numberLine, numberChar);
        numberLine = trioLong.second;
        numberChar = trioLong.third;
        spaceMarine.setHealth(trioLong.first);

        pair = readCharacter(',', reader, numberLine, numberChar);
        numberLine = pair.first;
        numberChar = pair.second;

        // read SpaceMarine.height
        pair = readAttribut("height", reader, numberLine, numberChar);
        numberLine = pair.first;
        numberChar = pair.second;

        Trio <Integer, Integer, Integer> trioInt = readInteger(reader, numberLine, numberChar);
        numberLine = trioInt.second;
        numberChar = trioInt.third;
        spaceMarine.setHeight(trioInt.first);

        pair = readCharacter(',', reader, numberLine, numberChar);
        numberLine = pair.first;
        numberChar = pair.second;

        // read SpaceMarine.category
        pair = readAttribut("category", reader, numberLine, numberChar);
        numberLine = pair.first;
        numberChar = pair.second;

        trioStr = readWord(reader, numberLine, numberChar);
        numberLine = trioStr.second;
        numberChar = trioStr.third;
        switch(trioStr.first.toUpperCase()) {
            case ("ASSAULT"):
                spaceMarine.setCategory(AstartesCategory.ASSAULT);
                break;
            case ("CHAPLAIN"):
                spaceMarine.setCategory(AstartesCategory.CHAPLAIN);
                break;
            case ("HELIX"):
                spaceMarine.setCategory(AstartesCategory.HELIX);
                break;
            default:
                throw new IllegalArgumentException("Wrong: incorrect category\n" +
                        "AstartessCategory: \"ASSAULT\", \"CHAPLAIN\", \"HELIX\"\n" +
                        "Line's number: " + numberLine + "\n" +
                        "Char's number: " + numberChar);
        }

        pair = readCharacter(',', reader, numberLine, numberChar);
        numberLine = pair.first;
        numberChar = pair.second;

        // read SpaceMarine.meleeWeapon
        pair = readAttribut("meleeWeapon", reader, numberLine, numberChar);
        numberLine = pair.first;
        numberChar = pair.second;

        trioStr = readWord(reader, numberLine, numberChar);
        numberLine = trioStr.second;
        numberChar = trioStr.third;
        switch(trioStr.first.toUpperCase()) {
            case ("CHAIN_SWORD"):
                spaceMarine.setMeleeWeapon(MeleeWeapon.CHAIN_SWORD);
                break;
            case ("POWER_SWORD"):
                spaceMarine.setMeleeWeapon(MeleeWeapon.POWER_SWORD);
                break;
            case ("CHAIN_AXE"):
                spaceMarine.setMeleeWeapon(MeleeWeapon.CHAIN_AXE);
                break;
            case ("MANREAPER"):
                spaceMarine.setMeleeWeapon(MeleeWeapon.MANREAPER);
                break;
            case ("POWER_FIST"):
                spaceMarine.setMeleeWeapon(MeleeWeapon.POWER_FIST);
                break;
            default:
                throw new IllegalArgumentException("Wrong: incorrect melee weapon\n" +
                        "MeleeWeapon: \"CHAIN_SWORD\", \"POWER_SWORD\", \"CHAIN_AXE\", \"MANREAPER\", \"POWER_FIST\"\n"  +
                        "Line's number: " + numberLine + "\n" +
                        "Char's number: " + numberChar);
        }

        pair = readCharacter(',', reader, numberLine, numberChar);
        numberLine = pair.first;
        numberChar = pair.second;

        Chapter chapter = new Chapter();
        // read SpaceMarine.chapter
        pair = readAttribut("chapter", reader, numberLine, numberChar);
        numberLine = pair.first;
        numberChar = pair.second;

        pair = readCharacter('{', reader, numberLine, numberChar);
        numberLine = pair.first;
        numberChar = pair.second;

        // read SpaceMarine.chapter.name
        pair = readAttribut("name", reader, numberLine, numberChar);
        numberLine = pair.first;
        numberChar = pair.second;

        trioStr = readWord(reader, numberLine, numberChar);
        numberLine = trioStr.second;
        numberChar = trioStr.third;
        chapter.setName(normalise(trioStr.first));

        pair = readCharacter(',', reader, numberLine, numberChar);
        numberLine = pair.first;
        numberChar = pair.second;

        // read SpaceMarine.parentLegion
        pair = readAttribut("parentLegion", reader, numberLine, numberChar);
        numberLine = pair.first;
        numberChar = pair.second;

        trioStr = readWord(reader, numberLine, numberChar);
        numberLine = trioStr.second;
        numberChar = trioStr.third;
        chapter.setParentLegion(normalise(trioStr.first));

        pair = readCharacter(',', reader, numberLine, numberChar);
        numberLine = pair.first;
        numberChar = pair.second;

        // read SpaceMarine.marinesCount
        pair = readAttribut("marinesCount", reader, numberLine, numberChar);
        numberLine = pair.first;
        numberChar = pair.second;

        trioInt = readInteger(reader, numberLine, numberChar);
        numberLine = trioInt.second;
        numberChar = trioInt.third;
        chapter.setMarinesCount(trioInt.first);

        pair = readCharacter(',', reader, numberLine, numberChar);
        numberLine = pair.first;
        numberChar = pair.second;

        // read SpaceMarine.world
        pair = readAttribut("world", reader, numberLine, numberChar);
        numberLine = pair.first;
        numberChar = pair.second;

        trioStr = readWord(reader, numberLine, numberChar);
        chapter.setWorld(normalise(trioStr.first));
        numberLine = trioStr.second;
        numberChar = trioStr.third;

        pair = readCharacter('}', reader, numberLine, numberChar);
        numberLine = pair.first;
        numberChar = pair.second;

        spaceMarine.setChapter(chapter);

        pair = readCharacter('}', reader, numberLine, numberChar);
        numberLine = pair.first;
        numberChar = pair.second;

        return new Trio<>(spaceMarine, numberLine, numberChar);
    }
}