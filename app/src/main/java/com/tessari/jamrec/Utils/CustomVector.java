package com.tessari.jamrec.Utils;

import java.util.Vector;

public class CustomVector<E> extends Vector<E> {

    // in sola lettura
    @Override
    public synchronized Object[] toArray(){
        return elementData;
    }


}