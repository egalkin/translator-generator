package ru.ifmo.galkin.utils;

import java.util.stream.IntStream;

public class MathUtils {
    public static int countFactorial(Integer n) {
        return n == 0 ? 1 : IntStream.range(1, n+1).reduce((a, b) -> a * b).getAsInt();
    }
}
