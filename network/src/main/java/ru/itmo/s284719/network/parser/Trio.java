package ru.itmo.s284719.network.parser;

/**
 * Package with three elements.
 *
 * @param <T> the first element.
 * @param <S> the second element.
 * @param <U> the third element.
 * @version 0.4
 * @author Danhout.
 */
public class Trio<T, S, U> {
    /**
     * The first element.
     */
    public T first;
    /**
     * The second element.
     */
    public S second;
    /**
     * The third element.
     */
    public U third;

    /**
     * Constructor with three elements.
     *
     * @param t the first element.
     * @param s the second element.
     * @param u the third element.
     */
    public Trio(T t, S s, U u) {
        first = t;
        second = s;
        third = u;
    }
}
