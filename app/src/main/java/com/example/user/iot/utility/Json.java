package com.example.user.iot.utility;

import com.example.user.iot.controller.MainActivity;

import java.io.IOException;
import java.io.InputStream;

/**
 * Created by NicoMac on 29/05/17.
 */

public class Json {
    public static String loadJSONFromAsset() {
        String json = null;
        try {
            InputStream is = MainActivity.context.getAssets().open("ConfigurationParameters");
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            json = new String(buffer, "UTF-8");
        } catch (IOException ex) {
            ex.printStackTrace();
            return null;
        }
        return json;
    }
}
