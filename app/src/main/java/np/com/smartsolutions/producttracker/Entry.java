package np.com.smartsolutions.producttracker;

import org.json.JSONException;
import org.json.JSONObject;

public class Entry {

    JSONObject jsonObject;

    public Entry(String json) throws JSONException {
        jsonObject = new JSONObject(json);
    }
    public Entry(JSONObject jsonObject) throws JSONException {
        this.jsonObject = jsonObject;
    }
    public String get(String key) throws JSONException {
        return jsonObject.getString(key);
    }

    public String getJson() {
        return jsonObject.toString();
    }
}
