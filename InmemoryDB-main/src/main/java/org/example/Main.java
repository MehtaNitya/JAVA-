package org.example;


import org.example.entity.Command;
import org.example.service.CommandService;
import org.example.service.DbService;
import org.example.service.IDbservice;

import java.util.Scanner;

public class Main {

    public static void main(String[] args) {

        IDbservice dbService = new DbService();
        CommandService commandService = new CommandService();

        if (args.length > 0 && "demo".equalsIgnoreCase(args[0])) {
            runMultiThreadDemo(dbService, commandService);
            return;
        }

        System.out.println("================================");
        System.out.println(" In-Memory DB Service Started ");
        System.out.println("================================");

        Scanner scanner = new Scanner(System.in);
        while (true) {
            if (!scanner.hasNextLine()) {
                break;
            }
            String input = scanner.nextLine();

            try {
                System.out.println("> " + input);

                Command command = commandService.parse(input);

                switch (command.type) {

                    case PUT:
                        if (command.ttl > 0) {
                            dbService.put(
                                    command.key,
                                    parseValue(command.rawValue),
                                    command.ttl
                            );
                        } else {
                            dbService.put(
                                    command.key,
                                    parseValue(command.rawValue)
                            );
                        }
                        System.out.println("OK");
                        break;

                    case GET:
                        Object value = dbService.get(command.key);
                        System.out.println("VALUE = " + value);
                        break;

                    case DELETE:
                        dbService.delete(command.key);
                        System.out.println("DELETED");
                        break;

                    case EXIT:
                        System.out.println("Shutting down DB...");
                        return;

                    case START:
                        dbService.start();
                        System.out.println("DB started");
                        break;

                    case STOP:
                        dbService.stop();
                        System.out.println("DB stopped");
                        break;
                }

            } catch (Exception e) {
                System.out.println("ERROR: " + e.getMessage());
            }
        }
    }

    private static Object parseValue(String value) {
        try { return Integer.parseInt(value); } catch (Exception ignored) {}
        try { return Double.parseDouble(value); } catch (Exception ignored) {}
        if ("true".equalsIgnoreCase(value) || "false".equalsIgnoreCase(value)) {
            return Boolean.parseBoolean(value);
        }
        return value;
    }

    private static void runMultiThreadDemo(IDbservice dbService, CommandService commandService) {
        String[][] commandBatches = {
                {"PUT 1 hello", "PUT 2 100", "GET 1", "DELETE 2"},
                {"PUT 3 world 2000", "GET 3", "PUT 4 42", "GET 4"},
                {"GET 2", "DELETE 1", "GET 1"}
        };

        Thread[] threads = new Thread[commandBatches.length];
        for (int i = 0; i < commandBatches.length; i++) {
            final int index = i;
            threads[i] = new Thread(() -> {
                for (String input : commandBatches[index]) {
                    try {
                        Command command = commandService.parse(input);
                        executeCommand(dbService, command);
                    } catch (Exception e) {
                        System.out.println("THREAD " + index + " ERROR: " + e.getMessage());
                    }
                }
            });
            threads[i].start();
        }

        for (Thread thread : threads) {
            try {
                thread.join();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return;
            }
        }
    }

    private static void executeCommand(IDbservice dbService, Command command) {
        switch (command.type) {
            case PUT:
                if (command.ttl > 0) {
                    dbService.put(command.key, parseValue(command.rawValue), command.ttl);
                } else {
                    dbService.put(command.key, parseValue(command.rawValue));
                }
                break;
            case GET:
                dbService.get(command.key);
                break;
            case DELETE:
                dbService.delete(command.key);
                break;
            case START:
                dbService.start();
                break;
            case STOP:
                dbService.stop();
                break;
            case EXIT:
                break;
        }
    }
}
