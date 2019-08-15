package com.tessari.jamrec.Save;

public interface Savable<T> {
    T save();

    void restore(T restoreObject);
}
