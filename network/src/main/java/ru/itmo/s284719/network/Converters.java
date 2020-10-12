package ru.itmo.s284719.network;

import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.time.ZoneId;

/**
 * The {@code Converters} class contains static methods for registering ZoneId converter.
 */
public class Converters {

    /** The specific genericized type for {@code ZoneId}. */
    public static final Type ZONE_ID_TYPE = new TypeToken<ZoneId>(){}.getType();

    /**
     * Registers the {@link ZoneId} converter.
     * @param builder The GSON builder to register the converter with.
     * @return A reference to {@code builder}.
     */
    public static GsonBuilder registerZoneId(GsonBuilder builder) {
        if (builder == null) { throw new NullPointerException("builder cannot be null"); }

        builder.registerTypeAdapter(ZONE_ID_TYPE, new ZoneIdConverter());

        return builder;
    }
}
