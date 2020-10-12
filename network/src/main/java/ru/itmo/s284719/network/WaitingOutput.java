package ru.itmo.s284719.network;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import static java.lang.System.err;
import static java.lang.System.out;

public interface WaitingOutput {
    static void wait(String prefixString) {
        out.print(prefixString);
        out.flush();
        try {
            for (int i = 0; i < 3; ++i) {
                Thread.sleep(500);
                out.print('.');
                out.flush();
            }
            Thread.sleep(500);
        } catch (InterruptedException e) {
            err.println("Sleeping thread was interrupted");
        }
        out.print("\r");
        out.flush();
    }
}
