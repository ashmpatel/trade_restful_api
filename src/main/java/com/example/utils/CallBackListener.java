package com.example.utils;

// Callback used by the memory mapped file reader
public interface CallBackListener<T> {

    T callBack(String data);

}
