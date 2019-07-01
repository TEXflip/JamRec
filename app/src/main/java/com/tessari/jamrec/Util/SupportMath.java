package com.tessari.jamrec.Util;

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

    public static double floorModD(double a, double b) {
        return (Math.floor(a / b) * b);
    }

    public static double map(double n, double start1, double stop1, double start2, double stop2) {
        return ((n - start1) / (stop1 - start1)) * (stop2 - start2) + start2;
    }

    public static float map(float n, float start1, float stop1, float start2, float stop2) {
        return ((n - start1) / (stop1 - start1)) * (stop2 - start2) + start2;
    }

    public static double constraint(double n, double min, double max) {
        return n < min ? min : n > max ? max : n;
    }

    public static float constraint(float n, float min, float max) {
        return n < min ? min : n > max ? max : n;
    }

    public static boolean isBetween(float n, float min, float max){
        return n > min && n < max;
    }
}
