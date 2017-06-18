package au.com.stepglobal.utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.lang.reflect.Modifier;

/**
 * Configure and center a Gson instance.
 *
 * Created by hiten.bahri on 6/18/2017.
 */
public class GsonFactory {
    private static Gson gson;

    /**
     * @return Get our default gson implementation.
     */
    public static Gson getGson() {
        if(gson == null) {
            gson = new GsonBuilder()
                    .excludeFieldsWithModifiers(Modifier.FINAL, Modifier.TRANSIENT, Modifier.STATIC)
                    .create();
        }
        return gson;
    }
}
