package np.com.smartsolutions.producttracker;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;

public class Entry {
    JSONObject jsonObject;
    boolean hasOrders;
    boolean hasReturns;

    public Entry(JSONObject jsonObject) throws JSONException, ParseException {
        this.jsonObject = jsonObject;
        if (!(jsonObject.length() > 1)) {
            if (jsonObject.names().getString(0).equals(Constants.JSON_PROD_ORDERS))
                hasOrders = true;
            else if (jsonObject.names().getString(0).equals(Constants.JSON_PROD_RETURNS))
                hasReturns = true;
        }
        else {
            hasOrders = hasReturns = true;
        }
    }

    public Entry(JSONObject orderObject, JSONObject returnObject) throws JSONException {
        jsonObject = new JSONObject();
        jsonObject.put(Constants.JSON_PROD_ORDERS, orderObject.toString());
        jsonObject.put(Constants.JSON_PROD_RETURNS, returnObject.toString());
    }

    public String getFromOrder(String key) throws JSONException {
        return jsonObject.getJSONObject(Constants.JSON_PROD_ORDERS).getString(key);
    }

    public String getFromReturn(String key) throws JSONException {
        return jsonObject.getJSONObject(Constants.JSON_PROD_RETURNS).getString(key);
    }

    public String getJson() {
        return jsonObject.toString();
    }
}
