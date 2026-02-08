package org.example.service;

public interface IDbservice {

    void put(Integer key , Object data , long ttl ) ;
    Object get(Integer key) ;
    void delete(Integer key) ;
    void put(Integer key, Object data)  ;
    void start();
    void stop();

}
