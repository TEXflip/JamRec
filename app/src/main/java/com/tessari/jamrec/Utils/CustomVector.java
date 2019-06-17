package com.tessari.jamrec.Utils;

public class CustomVector {


    private short[] data;
    private int length = 0;

    //Constructors
    public CustomVector() {
        data = new short[10];
    }

    public CustomVector(int initialCapacity) {
        if (initialCapacity < 10)
            initialCapacity = 10;
        data = new short[initialCapacity];
    }

    //Methods
    public int size() {
        return length;
    }

    public void ensureCapacity(final int capacity) {
        if (capacity <= data.length) return;
        short[] newData = new short[capacity];
        System.arraycopy(data, 0, newData, 0, data.length);
        data = newData;
    }

    public void add(final short value) {
        if (length == data.length)
            ensureCapacity(data.length + data.length / 2); // Grow by approx. 1.5x
        data[length] = value;
        ++length;
    }

    public void add(final int index, final short value) {
        if (index >= length || index < 0)
            throw new ArrayIndexOutOfBoundsException(index);
        if (length == data.length)
            ensureCapacity(data.length + data.length / 2); // Grow by approx. 1.5x
        for (int i = length; i > index; --i)
            data[i] = data[i - 1];
        data[index] = value;
        ++length;
    }

    public void set(final int index, final short value) {
        if (index >= length)
            throw new ArrayIndexOutOfBoundsException(index);
        data[index] = value; //if (index<0) or (index>data.length), IndexOutOf. is thrown
    }

    public short get(final int index) {
        if (index >= length)
            throw new ArrayIndexOutOfBoundsException(index);
        return data[index]; //if (index<0) or (index > data.length), IndexOutOf. is thrown
    }

    public void remove() { // Remove last element
        if (length == 0) return;
        --length;
    }

    public short remove(int index) { // Removes and returns the removed value
        final short result = get(index);
        --length;
        for (; index < length; ++index)
            data[index] = data[index + 1];
        return result;
    }

    public void clear() { // Make empty
        length = 0;
    }

    public short[] toArray(){
        return data;
    }
}
