package org.example.entity;

public class Entry<T>{
    public T value ;
    public long expiryTime ;

    public Entry(T data, long ttl) {
        this.value = data ;
        this.expiryTime = System.currentTimeMillis() +  ttl;
    }

    public Entry(T data) {
        this.value = data ;
        this.expiryTime =  -1 ;
    }

    public boolean isExpired(){
        if (expiryTime == -1){
            return false ;
        }
        return System.currentTimeMillis() > expiryTime ;
    }
}
