package org.example.service;

import org.example.entity.Command;
import org.example.entity.CommandType;
import org.example.exception.InvalidCommandException;
import org.example.exception.InvalidTTLException;



public class CommandService {

    public Command parse(String command) {

        if (command == null || command.trim().isEmpty()) {
            throw new InvalidCommandException("Command cannot be empty");
        }

        String[] arr = command.trim().split("\\s+");
        CommandType type = parseCommandType(arr);
        Command cmd = new Command();
        cmd.type = type ;


        switch (type) {
            case PUT:  parsePut(arr , cmd);
            break;
            case GET , DELETE :
                validateArgCount(arr, 2, type + " requires exactly 1 argument");
                parseKey(arr , cmd);
            break;
            case START , STOP  , EXIT:
                validateNoArgs(arr, type);
                break ;
            default:
                throw new InvalidCommandException("Unknown command type");


        }

        return cmd  ;

    }

    void parseKey(String  [] arr , Command command) {

        try{
            int key = Integer.parseInt(arr[1]);
            command.key = key;
        }
        catch (NumberFormatException e){
            throw new InvalidCommandException("key value has to be numeric");
        }


    }

    void parsePut(String[] arr , Command command) {

        validateArgCount(arr, 3, 4, "PUT requires 2 or 3 arguments");
        parseKey(arr , command);
        String value = arr[2];
        command.rawValue = value;
        if (arr.length == 4) {
          parseTtl(arr , command);
        }

    }

    void parseTtl(String[] arr , Command command) {
        try {
            long ttl = Long.parseLong(arr[3]);
            if (ttl <= 0) {
                throw new InvalidTTLException("TTL must be positive");
            }
            command.ttl = ttl;
        }
        catch (NumberFormatException e){
            throw new InvalidTTLException("TTL must be numeric");
        }

    }

    private CommandType parseCommandType(String[] arr) {
        if (arr.length == 0) {
            throw new InvalidCommandException("Command cannot be empty");
        }
        try {
            return CommandType.valueOf(arr[0].toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new InvalidCommandException("Unknown command type");
        }
    }

    private void validateNoArgs(String[] arr, CommandType type) {
        if (arr.length != 1) {
            throw new InvalidCommandException(type + " does not take arguments");
        }
    }

    private void validateArgCount(String[] arr, int expected, String message) {
        if (arr.length != expected) {
            throw new InvalidCommandException(message);
        }
    }

    private void validateArgCount(String[] arr, int min, int max, String message) {
        if (arr.length < min || arr.length > max) {
            throw new InvalidCommandException(message);
        }
    }

}
