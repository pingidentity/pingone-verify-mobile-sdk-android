package com.pingidentity.sdk.pingoneverify.ui.utils;

import android.content.Context;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.pingidentity.sdk.pingoneverify.models.Constants;
import com.pingidentity.sdk.pingoneverify.ui.Country;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class CountryUtil {

    public static List<Country> getCountryCodes(Context context) {
        String json;
        try {
            InputStream stream = context.getAssets().open(Constants.COUNTRIES_FILE_NAME);
            int size = stream.available();
            byte[] buffer = new byte[size];
            stream.read(buffer);
            stream.close();
            json = new String(buffer, StandardCharsets.UTF_8);
        } catch (IOException ex) {
            ex.printStackTrace();
            return new ArrayList<>();
        }
        Type listType = new TypeToken<ArrayList<Country>>() {}.getType();
        return new Gson().fromJson(json, listType);
    }
}
