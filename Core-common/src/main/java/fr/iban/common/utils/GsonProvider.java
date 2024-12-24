package fr.iban.common.utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class GsonProvider {

    private static final Gson GSON = new GsonBuilder()
            .disableHtmlEscaping()
            .create();

    public static Gson getGson() {
        return GSON;
    }

}