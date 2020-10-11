package network;

import static java.lang.System.out;

public interface WaitingOutput {
    public static void wait(String prefixString) {
        out.print(prefixString);
        out.flush();
        try {
            for (int i = 0; i < 3; ++i) {
                Thread.sleep(500);
                out.print('.');
                out.flush();
            }
            Thread.sleep(500);
        } catch (InterruptedException e) { /*//for logging.//e2.printStacktrace();*/}
        out.print("\r");
        out.flush();
    }
}
