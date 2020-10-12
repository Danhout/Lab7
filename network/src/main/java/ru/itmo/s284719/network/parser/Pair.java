package ru.itmo.s284719.network.parser;

import java.io.Serializable;

/**
 * Package with two elements.
 *
 * @param <T> the first element.
 * @param <S> the second element.
 * @version 0.4
 * @author Danhout.
 */
public class Pair<T extends Comparable<T>, S extends Comparable<S>> implements Comparable<Pair<T, S>>, Serializable {
    /**
     * The first element.
     */
    public T first;
    /**
     * The second element.
     */
    public S second;

    /**
     * Constructor with two parameters.
     *
     * @param t the first element.
     * @param s the second element.
     */
    public Pair(T t, S s) {
        first = (T) t;
        second = (S) s;
    }

    @Override
    public int compareTo(Pair<T, S> o) {
        int compareFirst = first.compareTo(o.first);
        if (compareFirst != 0)
            return compareFirst;
        else
            return second.compareTo(o.second);
    }

    public int compareToFirst(Pair<T, S> o) {
        return first.compareTo(o.first);
    }

    public int compareToSecond(Pair<T, S> o) {
        return second.compareTo(o.second);
    }

    @Override
    public String toString() {
        return "Pair: {first: " + first + ", second: " + second + "}";
    }
}
