package np.com.smartsolutions.producttracker;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

public class MainActivity extends AppCompatActivity {

    public static final String TAG = MainActivity.class.getSimpleName();

    ListView mListView;
    ListViewAdapter mAdapter;
    ArrayList<HashMap<String, String>> mEntries;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mListView = (ListView) findViewById(R.id.list_view);
        mAdapter = new ListViewAdapter(this);
        mListView.setAdapter(mAdapter);
        mEntries = new ArrayList<>();
    }

    @Override
    protected void onResume() {
        super.onResume();

        new GetEntries().execute();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();


        return super.onOptionsItemSelected(item);
    }


    private class GetEntries extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            if (!ServiceHandler.isOnline(MainActivity.this)) {
                new AlertDialog.Builder(MainActivity.this)
                        .setTitle("No Connection Detected")
                        .setMessage("Please check your internet connection and try again.")
                        .setNeutralButton("OK", null)
                        .show();
                this.cancel(true);
            }
        }

        @Override
        protected Void doInBackground(Void... params) {
            String response;
            // Get json from server
            try {
                HashMap<String, String> postParams = new HashMap<>();
                postParams.put(Constants.GET_ENTRIES, "");
                response = ServiceHandler.performPostCall(Constants.URL, postParams);

                Log.d(TAG, "Response: " + response);

                // Parse json data
                JSONObject jsonObject = new JSONObject(response);
                // Get products list
                JSONArray jsonProducts = jsonObject.getJSONArray("products");
                ArrayList<String> products = new ArrayList<>();
                for(int i = 0; i < jsonProducts.length(); i++) {
                    products.add(jsonProducts.getString(i));
                }
                // Add other columns
                products.add("user_id");
                products.add("date");
                products.add("edited_time");
                // Get entries
                ArrayList<HashMap<String, String>> entries = new ArrayList<>();
                JSONArray jsonEntries = jsonObject.getJSONArray("entries");
                int numEntries = jsonEntries.length();
                for (int i = 0; i < numEntries; i++) {
                    JSONObject object = (JSONObject) jsonEntries.get(i);
                    HashMap<String, String> objectMap = new HashMap<>();
                    for (int j = 0; j< products.size(); j++) {
                        String key = products.get(j);
                        objectMap.put(key, object.getString(key));
                    }
                    entries.add(objectMap);
                }
                mEntries = entries;
            } catch (Exception e) {
                e.printStackTrace();
                this.cancel(true);
            }

            return null;
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
            snackbar("Could not load data.");
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            mAdapter.updateEntries(mEntries);
            snackbar("Successfully updated.");
        }
    }

    public void snackbar(String messege) {
        Snackbar.make(findViewById(R.id.coordinator_layout), messege, Snackbar.LENGTH_LONG).show();
    }

}
