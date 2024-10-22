package fr.alexpado.cgc.heplers;

public class StringHelper {

    public static int count(String str, String search) {

        int count     = 0;
        int lastIndex = 0;

        while (lastIndex != -1) {

            lastIndex = str.indexOf(search, lastIndex);

            if (lastIndex != -1) {
                count++;
                lastIndex += search.length();
            }
        }

        return count;
    }

}
