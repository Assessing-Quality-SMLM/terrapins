package com.coxphysics.terrapins.models.squirrel.utils;

public class StringHelper {

    public static boolean contains(String key, String[] strings) {
        for (String s: strings) {
            if (s.equals(key)) return true;
        }
        return false;
    }

}
