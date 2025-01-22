package com.melonstudios.blockdisplay;

import com.melonstudios.melonlib.misc.Localizer;

import java.util.HashMap;
import java.util.Map;

class InternalLangKeyCache {
    private static final Map<String, String> cache = new HashMap<>();

    public static String translate(String key) {
        if (cache.containsKey(key)) return cache.get(key);
        String result = Localizer.translate(key);
        cache.put(key, result);
        return result;
    }

    public static void clear() {
        cache.clear();
    }
}
