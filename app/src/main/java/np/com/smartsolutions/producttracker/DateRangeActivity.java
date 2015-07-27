package np.com.smartsolutions.producttracker;


import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.HashMap;

public class DateRangeActivity extends AppCompatActivity {

    public static final String TAG = DateRangeActivity.class.getSimpleName();
    public static final String KEY_DATE1 = "date1";
    public static final String KEY_DATE2 = "date2";

    ListView mListView;
    ListViewAdapter mAdapter;
    SwipeRefreshLayout mSwipeRefresh;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findViewById(R.id.myFAB).setVisibility(View.INVISIBLE);
        mSwipeRefresh = (SwipeRefreshLayout) findViewById(R.id.swipeRefreshLayout);
        mSwipeRefresh.setEnabled(false);
        mSwipeRefresh.setColorSchemeResources(R.color.primary);
        // fix setRefreshing(true)
        mSwipeRefresh.setProgressViewOffset(false,
                (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, -48, getResources().getDisplayMetrics()),
                (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 16, getResources().getDisplayMetrics()));
        mListView = (ListView) findViewById(R.id.list_view);
        mAdapter = new ListViewAdapter(this);
        mListView.setAdapter(mAdapter);
        onNewIntent(getIntent());
    }

    @Override
    protected void onNewIntent(Intent intent) {
        String date1 = intent.getStringExtra(KEY_DATE1);
        String date2 = intent.getStringExtra(KEY_DATE2);
        Log.d(TAG, "Selected dates: " + date1 + " and " + date2);
        new GetSelectedEntries().execute(date1, date2);

        super.onNewIntent(intent);
    }

    public class GetSelectedEntries extends AsyncTask<String, Void, ArrayList<HashMap<String, String>>> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mSwipeRefresh.setRefreshing(true);
            if (!ServiceHandler.isOnline(DateRangeActivity.this)) {
                new AlertDialog.Builder(DateRangeActivity.this)
                        .setTitle("No Connection Detected")
                        .setMessage("Please check your internet connection and try again.")
                        .setPositiveButton("OK", null)
                        .show();
                this.cancel(true);
            }
        }

        @Override
        protected ArrayList<HashMap<String, String>> doInBackground(String... params) {
            String response;
            ArrayList<HashMap<String, String>> entries = new ArrayList<>();
            // Get json from server
            try {
                HashMap<String, String> postParams = new HashMap<>();
                postParams.put(Constants.DATE1, params[0]);
                postParams.put(Constants.DATE2, params[1]);
                response = ServiceHandler.performPostCall(Constants.URL, postParams);

                Log.d(TAG, "Response: " + response);

                entries = MainActivity.parseJsonEntries(DateRangeActivity.this, response);
            } catch (Exception e) {
                e.printStackTrace();
                this.cancel(true);
            }

            return entries;
        }

        @Override
        protected void onPostExecute(ArrayList<HashMap<String, String>> entries) {
            super.onPostExecute(entries);
            mAdapter.updateEntries(entries);
            mSwipeRefresh.setRefreshing(false);
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
            mSwipeRefresh.setRefreshing(false);
            snackbar("Could not load data.");
        }
    }

    public void snackbar(String messege) {
        Snackbar.make(findViewById(R.id.coordinator_layout), messege, Snackbar.LENGTH_LONG).show();
    }
}
