package np.com.smartsolutions.producttracker;

import android.content.Context;
import android.os.AsyncTask;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.widget.Toast;

import java.util.HashMap;

/**
 * Created by prabir on 7/27/15.
 */
public class AddProductAsync extends AsyncTask<String, Void, Void> {
    private static final String TAG = AddProductAsync.class.getSimpleName();
    Context mContext;

    public AddProductAsync(Context context) {
        mContext = context;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        if (!ServiceHandler.isOnline(mContext)) {

            new AlertDialog.Builder(mContext)
                    .setTitle("No Connection Detected")
                    .setMessage("Please check your internet connection and try again.")
                    .setPositiveButton("OK", null)
                    .show();
            this.cancel(true);
        } else {
            Toast.makeText(mContext, "Adding Product...", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected Void doInBackground(String... params) {
            String response = "";
            // Get json from server
            try {
                HashMap<String, String> postParams = new HashMap<>();
                postParams.put(Constants.ADD_PRODUCT, params[0]);
                response = ServiceHandler.performPostCall(Constants.URL, postParams);

                Log.d(TAG, "Response: " + response);
            } catch (Exception e) {
                e.printStackTrace();
                this.cancel(true);
            }finally {
                if (!response.contains("Success"))
                    cancel(true);
            }
        return null;
    }

    @Override
    protected void onCancelled() {
        super.onCancelled();
        Toast.makeText(mContext, "Product could not be added. Please try again. ", Toast.LENGTH_LONG).show();

    }

    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);
            Toast.makeText(mContext, "Product added successfully!", Toast.LENGTH_LONG).show();
    }
}
