package Lesson6.ConsoleChat;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

public class Client {
    private static Scanner reader;
    private static PrintWriter writer;
    private static String message;
    private static Scanner consoleReader;

    public static void main(String[] args) {
        try {
            Socket sock = new Socket("localhost", 8189);
            reader = new Scanner(sock.getInputStream());
            writer = new PrintWriter(sock.getOutputStream());
            System.out.println("Cоединение установлено...");
        } catch (IOException ex) {
            ex.printStackTrace();
        }

        new Thread(() -> {
            try {
                while (reader.hasNextLine()) {
                    message = reader.nextLine();
                    System.out.println("Получено от сервера: " + message);
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }).start();

        new Thread(() -> {
            while (true) {
                try {
                    consoleReader = new Scanner(System.in);
                    String msg = consoleReader.nextLine();
                    if (msg.equals("BYE")) {
                        System.exit(0);
                    } else {
                        writer.println(msg);
                        writer.flush();
                    }

                } catch (Exception ex) {
                    ex.printStackTrace();
                }

            }
        }).start();
    }

}