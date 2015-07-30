package np.com.smartsolutions.producttracker;

import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputLayout;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.jjoe64.graphview.series.DataPoint;
import com.squareup.timessquare.CalendarPickerView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import np.com.smartsolutions.producttracker.gcm.RegistrationIntentService;

public class MainActivity extends AppCompatActivity {

    public static final String TAG = MainActivity.class.getSimpleName();
    private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;

    RecyclerView mRecyclerView;
    EntriesRecyclerViewAdapter mAdapter;
    ArrayList<EntriesRecyclerViewAdapter.EntriesRecyclerItem> mEntries;
    SwipeRefreshLayout mSwipeRefresh;
    FloatingActionButton mAddFAB;
    Boolean firstLoad;
    View mFillerNothingHere;

    // For notifications
    private BroadcastReceiver mRegistrationBroadcastReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mRecyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mAdapter = new EntriesRecyclerViewAdapter(this);
        mRecyclerView.setAdapter(mAdapter);


        mEntries = new ArrayList<>();
        mSwipeRefresh = (SwipeRefreshLayout) findViewById(R.id.swipeRefreshLayout);

        mSwipeRefresh.setColorSchemeResources(R.color.primary);
        // fix setRefreshing(true)
        mSwipeRefresh.setProgressViewOffset(false,
                (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, -48, getResources().getDisplayMetrics()),
                (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 16, getResources().getDisplayMetrics()));
        mSwipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                new GetEntries().execute();
            }
        });

        mAddFAB = (FloatingActionButton) findViewById(R.id.myFAB);
        mAddFAB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ServiceHandler.isOnline(MainActivity.this)) {
                    Intent intent = new Intent(MainActivity.this, AddEntryActivity.class);
                    startActivity(intent);
                } else
                    noConnectionAlert();
            }
        });

        mRegistrationBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                SharedPreferences sharedPreferences =
                        PreferenceManager.getDefaultSharedPreferences(context);
                boolean sentToken = sharedPreferences
                        .getBoolean(Constants.SENT_TOKEN_TO_SERVER, false);
                if (sentToken) {
                    Log.d(TAG, "Token recieved and sent to server. GCM can be used.");
                } else {
                    Toast.makeText(MainActivity.this, "An error occurred while connecting to Google Play. Notifications will not work.", Toast.LENGTH_LONG).show();
                }
            }
        };

        mFillerNothingHere = findViewById(R.id.ll_filler);
        mFillerNothingHere.setVisibility(View.INVISIBLE);

        if (checkPlayServices()) {
            // Start IntentService to register this application with GCM.
            Intent intent = new Intent(this, RegistrationIntentService.class);
            startService(intent);
        }

        new GetEntries().execute();
        firstLoad = true;
    }

    public void addGraph() {

    }

    @Override
    protected void onResume() {
        super.onResume();
        LocalBroadcastManager.getInstance(this).registerReceiver(mRegistrationBroadcastReceiver,
                new IntentFilter(Constants.REGISTRATION_COMPLETE));

    }

    @Override
    protected void onPause() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mRegistrationBroadcastReceiver);
        super.onPause();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        int menu_id;
        if (new UserHandler(this).isAdmin())
            menu_id = R.menu.menu_main_admin;
        else
            menu_id = R.menu.menu_main;
        getMenuInflater().inflate(menu_id, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.date_range)
            selectDateRange();
        else if (id == R.id.admin)
            makeAdmin();
        else if (id == R.id.add_product)
            showAddProduct();
        else if (id == R.id.graph_setting)
            changeGraphSetting();
        else if (id == R.id.log_out)
            logout();

        return super.onOptionsItemSelected(item);
    }

    private void changeGraphSetting() {
        final Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        View view = getLayoutInflater().inflate(R.layout.graph_setting_dialog, null);
        final RadioGroup group = (RadioGroup) view.findViewById(R.id.radio_group);
        final SharedPreferences graphPrefs = getSharedPreferences(Constants.GRAPH_PREFS, 0);

        // Set initially selected button
        int selectedView = graphPrefs.getInt(Constants.GRAPH_VIEW, Constants.MONTH);
        int selectedRadioId;
        switch (selectedView) {
            case Constants.ALL:
                selectedRadioId = R.id.radio_all;
                break;
            case Constants.YEAR:
                selectedRadioId = R.id.radio_year;
                break;
            case Constants.WEEK:
                selectedRadioId = R.id.radio_week;
                break;
            case Constants.MONTH:
            default:
                selectedRadioId = R.id.radio_month;
                break;
        }
        ((RadioButton) view.findViewById(selectedRadioId)).toggle();

        view.findViewById(R.id.bu_cancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        view.findViewById(R.id.bu_set).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences.Editor editor = graphPrefs.edit();
                int setGraphView;
                switch (group.getCheckedRadioButtonId()) {
                    case R.id.radio_all:
                        setGraphView = Constants.ALL;
                        break;
                    case R.id.radio_year:
                        setGraphView = Constants.YEAR;
                        break;
                    case R.id.radio_week:
                        setGraphView = Constants.WEEK;
                        break;
                    case R.id.radio_month:
                    default:
                        setGraphView = Constants.MONTH;
                        break;
                }
                editor.putInt(Constants.GRAPH_VIEW, setGraphView);
                editor.commit();
                // refresh
                new GetEntries().execute();
                dialog.dismiss();
            }
        });
        dialog.setContentView(view);
        dialog.show();

        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);


    }

    private void showAddProduct() {
        final Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        View view = getLayoutInflater().inflate(R.layout.add_admin_dialog, null);
        final EditText input = (EditText) view.findViewById(R.id.edit_text_email);
        ((TextInputLayout) view.findViewById(R.id.text_input_layout)).setHint("Product Name");
        ((TextView) view.findViewById(R.id.title)).setText("Add Product");
        view.findViewById(R.id.bu_cancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        view.findViewById(R.id.bu_ok).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new AddProductAsync(MainActivity.this).execute(input.getText().toString().trim().replace("\\s", ""));
                dialog.dismiss();
            }
        });
        dialog.setContentView(view);
        dialog.show();

        Toast.makeText(this, "Product name must not contain spaces!", Toast.LENGTH_LONG).show();

        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

    }

    private void makeAdmin() {
        final Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        View view = getLayoutInflater().inflate(R.layout.add_admin_dialog, null);
        final EditText input = (EditText) view.findViewById(R.id.edit_text_email);
        ((TextInputLayout) view.findViewById(R.id.text_input_layout)).setHint("Email");
        view.findViewById(R.id.bu_cancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        view.findViewById(R.id.bu_ok).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new MakeAdmin().execute(input.getText().toString());
                dialog.dismiss();
            }
        });
        dialog.setContentView(view);
        dialog.show();

        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

    }

    private void logout() {
        new UserHandler(this).logout();
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
        finish();
    }

    private void selectDateRange() {
        final Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        final LayoutInflater inflater = getLayoutInflater();
        View view = inflater.inflate(R.layout.date_dialog, null);

        final CalendarPickerView calendarPickerView = (CalendarPickerView) view.findViewById(R.id.date_picker);
        Calendar calendar = Calendar.getInstance();
        calendar.set(calendar.get(Calendar.YEAR) - 1, calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
        Date before = new Date(calendar.getTimeInMillis());
        Date today = new Date(System.currentTimeMillis() + 86400000);
        calendarPickerView.init(before, today)
                .inMode(CalendarPickerView.SelectionMode.RANGE);
        calendarPickerView.setSelection(12); // scroll to bottom (current month)
        // Onclick cancel
        view.findViewById(R.id.bu_cancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        // Onclick ok
        view.findViewById(R.id.bu_ok).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                List<Date> dateList = calendarPickerView.getSelectedDates();
                if (dateList != null && dateList.size() > 0) {
                    DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
                    String start = dateFormat.format(dateList.get(0));
                    String end = dateFormat.format(dateList.get(dateList.size() - 1));
                    Intent intent = new Intent(MainActivity.this, DateRangeActivity.class);
                    intent.putExtra(DateRangeActivity.KEY_DATE1, start);
                    intent.putExtra(DateRangeActivity.KEY_DATE2, end);
                    intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                    startActivity(intent);

                    Log.d(TAG, "Start: " + start + ", End: " + end);
                }
                dialog.dismiss();
            }
        });
        dialog.setContentView(view);
        dialog.show();

    }

    private class GetEntries extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mSwipeRefresh.setRefreshing(true);
            mFillerNothingHere.setVisibility(View.INVISIBLE);
            if (!ServiceHandler.isOnline(MainActivity.this)) {
                noConnectionAlert();
                this.cancel(true);
            }
        }

        @Override
        protected Void doInBackground(Void... params) {
            String response;
            // Get json from server
            try {
                HashMap<String, String> postParams = new HashMap<>();
                postParams.put(Constants.GET_ENTRIES, "50");
                response = ServiceHandler.performPostCall(Constants.URL, postParams);

                Log.d(TAG, "Response: " + response);

                mEntries = parseJsonEntries(MainActivity.this, response);
                if (mEntries.size() > 1) { // add graph and load the rest of the list
                    /*
                    mEntries.add(0, new EntriesRecyclerViewAdapter.EntriesRecyclerItem(EntriesRecyclerViewAdapter.VIEW_GRAPH));
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                       //     mAdapter.updateEntries(mEntries);
                        }
                    });
                    mEntries.get(0).addDataPoints(getDataPoints(mEntries));
                    */
                    mEntries.add(0, new EntriesRecyclerViewAdapter.EntriesRecyclerItem(getDataPoints(mEntries)));
                }
            } catch (Exception e) {
                e.printStackTrace();
                this.cancel(true);
            }
            return null;
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
            mSwipeRefresh.setRefreshing(false);
            snackbar("Could not load data.");
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            mAdapter.updateEntries(mEntries);
            mSwipeRefresh.setRefreshing(false);
            if (!(mEntries.size() > 0))
                mFillerNothingHere.setVisibility(View.VISIBLE);
            if (!firstLoad)
                snackbar("Successfully updated.");
            firstLoad = !firstLoad;
        }
    }

    private void noConnectionAlert() {
        new AlertDialog.Builder(MainActivity.this)
                .setTitle("No Connection Detected")
                .setMessage("Please check your internet connection and try again.")
                .setPositiveButton("OK", null)
                .show();
    }

    @NonNull
    public static ArrayList<EntriesRecyclerViewAdapter.EntriesRecyclerItem> parseJsonEntries(Context context, String response)
            throws JSONException {
        // Parse json data
        JSONObject jsonObject = new JSONObject(response);
        // Get products list
        JSONArray jsonProducts = jsonObject.getJSONArray(Constants.JSON_CLASS_PRODUCTS);
        ArrayList<String> products = new ArrayList<>();
        for (int i = 0; i < jsonProducts.length(); i++) {
            products.add(jsonProducts.getString(i));
        }
        // Save products
        SharedPreferences.Editor editor = context.getSharedPreferences(Constants.COLUMN_PREFS, 0).edit();
        editor.putInt(Constants.NUM_PRODUCTS, products.size());
        for (int i = 0; i < products.size(); i++)
            editor.putString("" + i, products.get(i));
        editor.apply();

        // Add other columns
        products.addAll(Arrays.asList(Constants.ADDITIONAL_COLUMNS));

        // Get entries
        ArrayList<EntriesRecyclerViewAdapter.EntriesRecyclerItem> entries = new ArrayList<>();
        JSONArray jsonEntries = jsonObject.getJSONArray(Constants.JSON_CLASS_ENTRIES);
        int numEntries = jsonEntries.length();
        for (int i = 0; i < numEntries; i++) {
            entries.add(
                    new EntriesRecyclerViewAdapter.EntriesRecyclerItem(
                            new Entry((JSONObject) jsonEntries.get(i))));
        }
        return entries;
    }

    public static DataPoint[] getDataPoints(
            ArrayList<EntriesRecyclerViewAdapter.EntriesRecyclerItem> entries) {
        DateFormat serverFormat = new SimpleDateFormat("yyyy-MM-dd");

        // Count number of entries
        int numDataPoints = 0;
        for (EntriesRecyclerViewAdapter.EntriesRecyclerItem e : entries)
            if (e.viewType == EntriesRecyclerViewAdapter.VIEW_ENTRY)
                numDataPoints++;
        // Declare array
        DataPoint[] dataPoints = new DataPoint[numDataPoints];
        Log.d(TAG, "Data points: " + numDataPoints);

        try {
            int count = 0;
            for (int i = entries.size() - 1; i >= 0; i--) {
                EntriesRecyclerViewAdapter.EntriesRecyclerItem entryItem = entries.get(i);
                if (entryItem.viewType == EntriesRecyclerViewAdapter.VIEW_ENTRY) {
                    Entry e = entryItem.entry;
                    dataPoints[count++] = new DataPoint(
                            serverFormat.parse(e.get("date")),
                            Integer.parseInt(e.get("total")));
                }
            }
        } catch (JSONException | ParseException e) {
            e.printStackTrace();
        }
        return dataPoints;
    }

    public class MakeAdmin extends AsyncTask<String, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            Toast.makeText(MainActivity.this, "Sending request...", Toast.LENGTH_SHORT).show();
        }

        @Override
        protected Void doInBackground(String... params) {
            String response = "";
            // Get json from server
            try {
                HashMap<String, String> postParams = new HashMap<>();
                postParams.put(Constants.USER_OP, Constants.MAKE_ADMIN);
                postParams.put("email", params[0].trim());
                response = ServiceHandler.performPostCall(Constants.URL, postParams);

                Log.d(TAG, "Response: " + response);

            } catch (Exception e) {
                e.printStackTrace();
                this.cancel(true);
            } finally {
                if (!(response.contains("Admin added")))
                    this.cancel(true);
            }
            return null;
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
            Toast.makeText(MainActivity.this, "Error adding user as admin. Please try again.", Toast.LENGTH_LONG).show();
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            Toast.makeText(MainActivity.this, "Admin added successfully!", Toast.LENGTH_LONG).show();
        }
    }


    public void snackbar(String messege) {
        Snackbar.make(findViewById(R.id.coordinator_layout), messege, Snackbar.LENGTH_LONG).show();
    }

    /**
     * Check the device to make sure it has the Google Play Services APK. If
     * it doesn't, display a dialog that allows users to download the APK from
     * the Google Play Store or enable it in the device's system settings.
     */
    private boolean checkPlayServices() {
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
                GooglePlayServicesUtil.getErrorDialog(resultCode, this,
                        PLAY_SERVICES_RESOLUTION_REQUEST).show();
            } else {
                Log.i(TAG, "This device is not supported.");
                snackbar("Push notifications not supported on this device.");
                finish();
            }
            return false;
        }
        return true;
    }
}
