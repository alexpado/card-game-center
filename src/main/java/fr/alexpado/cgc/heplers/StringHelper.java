package fr.alexpado.cgc.heplers;

import java.util.Arrays;

public class StringHelper {

    public static int count(String str, String search) {

        return Arrays.stream(str.split(" "))
                     .filter(s -> s.equalsIgnoreCase(search))
                     .mapToInt(s -> 1)
                     .sum();
    }

}
