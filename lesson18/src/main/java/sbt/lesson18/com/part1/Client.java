package sbt.lesson18.com.part1;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sbt.lesson18.com.utils.Receiver;
import sbt.lesson18.com.utils.Sender;
import sbt.lesson18.com.utils.SocketReader;
import sbt.lesson18.com.utils.SocketSender;

import java.io.IOException;
import java.net.Socket;


/**
 * Клиент: отвечает за интерфейс обработки действий клиента по общению с сервером
 */
public class Client {

    private static final Logger LOGGER = LoggerFactory.getLogger(Client.class);

    private static final int PORT = 1234;
    private static final String HOST = "localhost";

    public static void main(String[] args) throws IOException {
        try (
                Socket socket = new Socket(HOST, PORT);
                Receiver consoleReader = new SocketReader(System.in);
                Sender sender = new SocketSender(socket.getOutputStream());
                Receiver reader = new SocketReader(socket.getInputStream())
        ) {
            LOGGER.info("Connected");
            boolean answer = false;
            while (!answer) {
                sender.send(consoleReader.readString());
                answer = reader.readBool();
                LOGGER.info("Server answer: {}", answer);
            }
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }
}
