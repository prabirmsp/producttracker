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
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.squareup.timessquare.CalendarPickerView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
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

    ListView mListView;
    ListViewAdapter mAdapter;
    ArrayList<HashMap<String, String>> mEntries;
    SwipeRefreshLayout mSwipeRefresh;
    FloatingActionButton mAddFAB;
    Boolean firstLoad;

    // For notifications
    private BroadcastReceiver mRegistrationBroadcastReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mListView = (ListView) findViewById(R.id.list_view);
        mAdapter = new ListViewAdapter(this);
        mListView.setAdapter(mAdapter);
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

            }
        });

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

        if (checkPlayServices()) {
            // Start IntentService to register this application with GCM.
            Intent intent = new Intent(this, RegistrationIntentService.class);
            startService(intent);
        }

        new GetEntries().execute();
        firstLoad = true;
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
        else if (id == R.id.log_out)
            logout();

        return super.onOptionsItemSelected(item);
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

    /**
     * Using DatePicker
     **/
    /*
    private void selectDateRange() {
        final Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        final int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);

        final DatePickerDialog.OnDateSetListener endListener = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                date2 = Calendar.getInstance();
                date2.set(year, monthOfYear, dayOfMonth);
                Calendar now = Calendar.getInstance();
                if (date2.after(now) || date1.after(now))
                    new AlertDialog.Builder(MainActivity.this)
                            .setTitle("Invalid Dates")
                            .setMessage("Please pick dates from the past.")
                            .setPositiveButton("OK", null)
                            .show();
                else if (date2.before(date1)) {
                    new AlertDialog.Builder(MainActivity.this)
                            .setTitle("Invalid Dates")
                            .setMessage("Start date needs to be before the end date.")
                            .setPositiveButton("OK", null)
                            .show();
                } else {
                    snackbar("valid");
                }


            }
        };
        final DatePickerDialog.OnDateSetListener startListener = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                date1 = Calendar.getInstance();
                date1.set(year, monthOfYear, dayOfMonth);
                DatePickerDialog endPicker = new DatePickerDialog(MainActivity.this, endListener, year, monthOfYear, dayOfMonth);
                endPicker.setTitle("Select End Date");
                endPicker.show();
            }
        };
        DatePickerDialog datePickerDialog = new DatePickerDialog(this, startListener, year, month, day);
        datePickerDialog.setTitle("Select Start Date");
        datePickerDialog.show();
    } */

    private class GetEntries extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mSwipeRefresh.setRefreshing(true);
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
                postParams.put(Constants.GET_ENTRIES, "");
                response = ServiceHandler.performPostCall(Constants.URL, postParams);

                Log.d(TAG, "Response: " + response);

                mEntries = parseJsonEntries(MainActivity.this, response);
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
    public static ArrayList<HashMap<String, String>> parseJsonEntries(Context context, String response)
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
        String[] cols = {"user_id", "date", "edited_time", "total"};
        products.addAll(Arrays.asList(cols));

        // Get entries
        ArrayList<HashMap<String, String>> entries = new ArrayList<>();
        JSONArray jsonEntries = jsonObject.getJSONArray(Constants.JSON_CLASS_ENTRIES);
        int numEntries = jsonEntries.length();
        for (int i = 0; i < numEntries; i++) {
            JSONObject object = (JSONObject) jsonEntries.get(i);
            HashMap<String, String> objectMap = new HashMap<>();
            for (String s : products) {
                objectMap.put(s, object.getString(s));
            }
            entries.add(objectMap);
        }
        return entries;
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
