package com.tessari.jamrec.Save;

public interface Savable<T> {
    String getName();

    T save();

    void restore(T restoreObject);
}
