package es.joapingut.eshoe.dto;

import java.io.Serializable;

public class FixedFiFo<T> implements Serializable {

    private T[] data;

    private int size;

    private int head;
    private int tail;

    public FixedFiFo(int size){
        data = (T[]) new Object[size];
        head = 0;
        tail = 0;
    }

    public void push(T object){
        head += 1;
        if (head - tail >= size){
            tail += 1;
        }
        if (head >= size){
            head = 0;
        }
        if (tail >= size){
            tail = 0;
        }
        data[head] = object;
    }

    public T pop(){
        T result = data[tail];
        tail += 1;
        if (head - tail < 0){
            tail = head;
        }
        if (tail >= size){
            tail = 0;
        }
        return result;
    }

    public T peek(){
        return data[tail];
    }

    public void clear(){
        head = 0;
        tail = 0;
        data[0] = null;
    }
}
