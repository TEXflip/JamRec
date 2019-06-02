package com.tessari.jamrec.Utils;

/**
 * Classe Math custom per aggiungere le funzioni non presenti nella min API
 */
public class SupportMath {

    public static int floorDiv(int a, int b) {
        int r = a / b;
        if ((a ^ b) < 0 && (r * b != a))
            r--;
        return r;
    }

    public static int floorMod(double a, double b) {
        return (int) (Math.floor(a / b) * b);
    }
}
