package org.example.service;

import org.example.entity.Entry;
import org.example.exception.DatabaseStoppedException;
import org.example.exception.InvalidTTLException;
import org.example.exception.KeyNotFoundException;

import java.util.concurrent.ConcurrentHashMap;

public class DbService  implements IDbservice{
    private final ConcurrentHashMap<Integer , Entry<Object>> data ;
    private volatile boolean running;
    private final int cleanupTimeMs = 1000;

    public DbService() {
        data = new ConcurrentHashMap<>();
        running = true;
        startCleanupThread();
    }

    synchronized public void put(Integer key, Object data , long ttl) {
        ensureRunningForWrite();
        if (ttl <= 0) {
            throw new InvalidTTLException("TTL must be positive");
        }
        Entry<Object> ob = new Entry<>(data , ttl)  ;
        this.data.put(key , ob) ;
    }


    synchronized public  void put(Integer key, Object data) {
        ensureRunningForWrite();
        Entry<Object> ob = new Entry<>(data)  ;
        this.data.put(key , ob) ;
    }


    synchronized public  Object get(Integer key) {
        ensureRunningForRead();
        Entry<Object> entry = data.get(key);
        if (entry == null) {
            throw new KeyNotFoundException("Key not found");
        }
        if (entry.isExpired()) {
            data.remove(key);
            throw new KeyNotFoundException("Key not found or expired");
        }
        return entry.value ;
    }


    synchronized public  void delete(Integer key) {
        ensureRunningForWrite();
        if(data.containsKey(key)){
            data.remove(key) ;
        }
        else {
            throw new KeyNotFoundException("Key not found");
        }
    }

    public void start(){
        running = true ;
    }

    public void stop(){
        running = false ;
    }

    private void ensureRunningForWrite() {
        if(!running){
            throw new DatabaseStoppedException("Database is stopped");
        }
    }

    private void ensureRunningForRead() {
        if(!running){
            throw new DatabaseStoppedException("Database is stopped");
        }
    }

    private void startCleanupThread(){
        Thread thread = new Thread(() -> {
            while (true) {
                if (running) {
                    cleanupExpiredKeys();
                }
                try {
                    Thread.sleep(cleanupTimeMs);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return;
                }
            }
        });
        thread.setDaemon(true);
        thread.start();
    }

    private void cleanupExpiredKeys(){
        for (Integer key : data.keySet()) {
            Entry<Object> entry = data.get(key);
            if (entry != null && entry.isExpired()) {
                data.remove(key);
            }
        }
    }
}
