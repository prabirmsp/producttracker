package np.com.smartsolutions.producttracker;

import android.content.Context;
import android.content.SharedPreferences;

import org.json.JSONException;
import org.json.JSONObject;

public class UserHandler {

    Context mContext;
    SharedPreferences mPreferences;

    public static final String USER_PREFS = "user_prefs";

    public static final String IS_LOGGED_IN = "logged_in";
    public static final String NAME = "name";
    public static final String EMAIL = "email";
    public static final String UID = "uid";
    public static final String ADMIN = "admin";

    public UserHandler(Context context) {
        mContext = context;
        mPreferences = context.getSharedPreferences(USER_PREFS, 0);
    }

    public boolean isLoggedIn() {
        return mPreferences.getBoolean(IS_LOGGED_IN, false);
    }

    public void login(String name, String email, String uid, Boolean admin) {
        SharedPreferences.Editor editor = mPreferences.edit();
        editor.putString(NAME, name);
        editor.putString(EMAIL, email);
        editor.putString(UID, uid);
        editor.putBoolean(ADMIN, admin);
        editor.putBoolean(IS_LOGGED_IN, true);
        editor.commit();
    }

    public void logout() {
        mPreferences.edit().clear().commit();
    }

    public void loginFromJSON (String jsonString) throws JSONException {
        JSONObject object = new JSONObject(jsonString);
        String name = object.getString("name");
        String email = object.getString("email");
        String uid = object.getString("unique_id");
        Boolean admin = object.getString("admin").equals("1");
        login(name, email, uid, admin);
    }

    public String getName() {
        return mPreferences.getString(NAME, "unknown");
    }
    public String getUid() {
        return mPreferences.getString(UID, "unknown");
    }
    public boolean isAdmin() {
        return mPreferences.getBoolean(ADMIN, false);
    }
}
