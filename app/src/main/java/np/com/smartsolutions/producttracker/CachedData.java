package np.com.smartsolutions.producttracker;

import android.content.Context;

public class CachedData {
    private final static String PREF_CACHE = "pref_cache";
    private final static String CACHED_STRING = "cached_string";

    public static String load(Context context) {
        return context.getSharedPreferences(PREF_CACHE, 0).getString(CACHED_STRING, "");
    }

    public static void save(Context context, String s) {
        context.getSharedPreferences(PREF_CACHE, 0).edit().putString(CACHED_STRING, s).apply();
    }
}
