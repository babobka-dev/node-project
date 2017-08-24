package ru.babobka.nodeutils.network;

import ru.babobka.nodeutils.container.Container;
import ru.babobka.nodeutils.util.StreamUtil;

import java.io.Closeable;
import java.io.IOException;
import java.net.Socket;
import java.net.SocketException;

/**
 * Created by 123 on 12.07.2017.
 */
public class NodeConnection implements Closeable {

    private final Socket socket;
    private final StreamUtil streamUtil = Container.getInstance().get(StreamUtil.class);

    public NodeConnection(Socket socket) {
        if (socket == null) {
            throw new IllegalArgumentException("socket is null");
        } else if (socket.isClosed()) {
            throw new IllegalArgumentException("socket is closed");
        }
        this.socket = socket;
    }

    public void setReadTimeOut(int timeOutMillis) throws IOException {
        try {
            synchronized (socket) {
                socket.setSoTimeout(timeOutMillis);
            }
        } catch (SocketException e) {
            throw new IOException(e);
        }
    }

    public <T> T receive() throws IOException {
        return streamUtil.receiveObject(socket);
    }

    public void send(Object object) throws IOException {
        synchronized (socket) {
            streamUtil.sendObject(object, socket);
        }
    }


    public void close() {
        synchronized (socket) {
            if (!socket.isClosed()) {
                try {
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public boolean isClosed() {
        synchronized (socket) {
            return socket.isClosed();
        }
    }

    public int getServerPort() {
        synchronized (socket) {
            return socket.getPort();
        }
    }

    public int getLocalPort() {
        synchronized (socket) {
            return socket.getLocalPort();
        }
    }

    public String getHostName() {
        synchronized (socket) {
            return socket.getInetAddress().getCanonicalHostName();
        }
    }

    @Override
    public String toString() {
        synchronized (socket) {
            return socket.toString();
        }
    }
}
