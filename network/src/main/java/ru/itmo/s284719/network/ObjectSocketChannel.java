package ru.itmo.s284719.network;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;

/**
 * Abstract class with functions for transmitting objects to channel.
 */
public abstract class ObjectSocketChannel {
    /**
     * The size of byte's buffer for getting objects.
     */
    private static int BUFF_SIZE = 8192;

    /**
     * Send an object to a channel.
     *
     * @author Kirill Shahow.
     * @param channel the channel for transmitting objects.
     * @param object the object for sending to the channel.
     */
    public static void sendObject(SocketChannel channel, Object object) throws IOException {
        ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
        ObjectOutputStream objectOut = new ObjectOutputStream(byteOut);
        synchronized (objectOut) {
            objectOut.writeObject(object);
            objectOut.flush();
        }
        channel.write(ByteBuffer.wrap(byteOut.toByteArray()));
    }
    /**
     * Get an object from a channel.
     *
     * @author Kirill Shahow & Danhout.
     * @param channel the channel for transmitting objects.
     * @return an object for sending to the channel.
     */
    public static Object getObject(SocketChannel channel) throws IOException, ClassNotFoundException {
        ByteBuffer buffer = ByteBuffer.allocate(BUFF_SIZE);
        Selector selector = Selector.open();
        channel.configureBlocking(false);
        channel.register(selector, SelectionKey.OP_READ);
        while (selector.select() < 1) { continue; }
        synchronized (channel) {
            channel.read(buffer);
        }
        ByteArrayInputStream byteIn = new ByteArrayInputStream(buffer.array());
        ObjectInputStream objectIn = new ObjectInputStream(byteIn);
        return objectIn.readObject();
    }
}