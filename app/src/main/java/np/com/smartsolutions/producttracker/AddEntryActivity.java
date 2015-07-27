package np.com.smartsolutions.producttracker;


import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

public class AddEntryActivity extends AppCompatActivity {

    public static final String TAG = AddEntryActivity.class.getSimpleName();

    Button mDateButton;
    RecyclerView mRecyclerView;
    AddRecyclerViewAdapter mAdapter;
    ArrayList<String> mProducts;
    Button mAddButton;
    Button mCancelButton;
    Calendar mSelectedDate;
    DateFormat mLocalFormat;

    ArrayList<String> entries;
    ArrayList<String> keys;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_entry_dialog);

        mDateButton = (Button) findViewById(R.id.button_date);
        mAddButton = (Button) findViewById(R.id.button_add);
        mCancelButton = (Button) findViewById(R.id.button_cancel);
        mRecyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        LinearLayoutManager llm = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(llm);

        mCancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        onNewIntent(getIntent());

    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        SharedPreferences preferences = getSharedPreferences(Constants.COLUMN_PREFS, 0);
        int numProducts = preferences.getInt(Constants.NUM_PRODUCTS, 0);
        mProducts = new ArrayList<>(numProducts);
        for (int i = 0; i < numProducts; i++) {
            String s = preferences.getString("" + i, null);
            if (s != null)
                mProducts.add(s);
        }

        mAdapter = new AddRecyclerViewAdapter(this, mProducts);
        mRecyclerView.setAdapter(mAdapter);

        mSelectedDate = Calendar.getInstance();
        mLocalFormat = DateFormat.getDateInstance();
        mDateButton.setText(mLocalFormat.format(new Date(mSelectedDate.getTimeInMillis())));
        mDateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDatePicker();
            }
        });
        mAddButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addButtonClicked();
            }
        });


    }

    private void addButtonClicked() {
        if (ServiceHandler.isOnline(this)) {
            // Get all values entered
            entries = mAdapter.getEntries();
            keys = mProducts;
            for (int i = 0; i < entries.size(); i++)
                if (entries.get(i).equals(""))
                    entries.set(i, "0");

            // Add date
            keys.add("date");
            DateFormat serverFormat = new SimpleDateFormat("yyyy-MM-dd");
            entries.add(serverFormat.format(new Date(mSelectedDate.getTimeInMillis())));

            // Add username
            keys.add("user_id");
            UserHandler userHandler = new UserHandler(this);
            entries.add(userHandler.getName());

            // Add unique_id
            keys.add("user_unique_id");
            entries.add(userHandler.getUid());

            // param[0] is products list
            // param[1] is entries list
            new SubmitEntryToAdd().execute();
            finish();
        } else {
            new AlertDialog.Builder(this)
                    .setTitle("No Connection Detected")
                    .setMessage("Please check your internet connection and try again.")
                    .setPositiveButton("OK", null)
                    .show();
        }

    }

    private void showDatePicker() {
        DatePickerDialog dialog = new DatePickerDialog(
                AddEntryActivity.this,
                new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                        Calendar today = Calendar.getInstance();
                        Calendar selected = Calendar.getInstance();
                        selected.set(year, monthOfYear, dayOfMonth);
                        if (selected.after(today))
                            Toast.makeText(AddEntryActivity.this, "You cannot pick a date in the future.", Toast.LENGTH_LONG).show();
                        else {
                            mSelectedDate.set(year, monthOfYear, dayOfMonth);
                            mDateButton.setText(mLocalFormat.format(new Date(mSelectedDate.getTimeInMillis())));
                        }
                    }
                },
                mSelectedDate.get(Calendar.YEAR), mSelectedDate.get(Calendar.MONTH), mSelectedDate.get(Calendar.DAY_OF_MONTH));
        dialog.show();
    }

    public class SubmitEntryToAdd extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            Toast.makeText(AddEntryActivity.this, "Adding entry...", Toast.LENGTH_LONG).show();
        }

        @Override
        protected Void doInBackground(Void... params) {
            String response = "";
            // Get json from server
            try {
                JSONObject jsonObject = new JSONObject();
                jsonObject.put(Constants.JSON_CLASS_PRODUCTS, new JSONArray(keys));
                jsonObject.put(Constants.JSON_CLASS_ENTRIES, new JSONArray(entries));
                String jsonString = jsonObject.toString();
                Log.d(TAG, "Sending JSON: " + jsonString);

                HashMap<String, String> postParams = new HashMap<>();
                postParams.put(Constants.SEND_ENTRY, jsonString);
                response = ServiceHandler.performPostCall(Constants.URL, postParams);

                Log.d(TAG, "Response: " + response);

            } catch (Exception e) {
                e.printStackTrace();
                this.cancel(true);
            } finally {
                if (!(response.contains(Constants.ENTRY_ADDED)))
                    this.cancel(true);
            }
            return null;
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
            Toast.makeText(AddEntryActivity.this, "Error adding entry. Please try again.", Toast.LENGTH_LONG).show();
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            Toast.makeText(AddEntryActivity.this, "Entry added successfully!", Toast.LENGTH_LONG).show();
        }
    }
}
