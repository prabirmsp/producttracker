package np.com.smartsolutions.producttracker;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import org.json.JSONException;

import java.text.DateFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class EntryInfoActivity extends AppCompatActivity {

    private static final String TAG = EntryInfoActivity.class.getSimpleName();
    ArrayList<String> mProducts;
    Entry mEntry;
    Date mDate;
    ListView mListView;
    NumberFormat numberFormat;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_entry_info);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mListView = (ListView) findViewById(R.id.list_view);
        numberFormat = NumberFormat.getIntegerInstance();

        onNewIntent(getIntent());
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        handleIntent(intent);
    }

    private void handleIntent(Intent intent) {
        String json = intent.getStringExtra(EntriesRecyclerViewAdapter.KEY_ENTRY_JSON);

        // Get products list
        SharedPreferences prefs = getSharedPreferences(Constants.COLUMN_PREFS, 0);
        int numProducts = prefs.getInt(Constants.NUM_PRODUCTS, 0);
        mProducts = new ArrayList<>();
        for (int i = 0; i < numProducts; i ++) {
            mProducts.add(prefs.getString("" + i, ""));
        }

        try {
            mEntry = new Entry(json);
            DateFormat serverFormat = new SimpleDateFormat("yyyy-MM-dd");
            DateFormat localFormat = DateFormat.getDateInstance();
            mDate = serverFormat.parse(mEntry.get("date"));

            setTitle(localFormat.format(mDate));

            TextView userName = (TextView) findViewById(R.id.tv_user);
            userName.setText(mEntry.get("user_id"));

            TextView editTime = (TextView) findViewById(R.id.tv_edit_date);
            editTime.setText("On " + mEntry.get("edited_time"));

            TextView total = (TextView) findViewById(R.id.tv_total);
            total.setText(numberFormat.format(Integer.parseInt(mEntry.get("total"))));

            ListViewAdapter adapter = new ListViewAdapter();
            mListView.setAdapter(adapter);

        } catch (JSONException | ParseException e) {
            e.printStackTrace();
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_entry_info, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == android.R.id.home) {
            finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private class ListViewAdapter extends BaseAdapter {

        public ListViewAdapter () {
            super();
        }

        @Override
        public int getCount() {
            return mProducts.size();
        }

        @Override
        public String getItem(int position) {
            return mProducts.get(position);
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = View.inflate(EntryInfoActivity.this, R.layout.list_item_product, null);
            }
            String product = mProducts.get(position);
            // Log.d(TAG, "Product: " + product);
            TextView productName = (TextView) convertView.findViewById(R.id.tv_product_name);
            productName.setText(product);

            try {
                TextView productValue = (TextView) convertView.findViewById(R.id.tv_product_value);
                productValue.setText(numberFormat.format(Integer.parseInt(mEntry.get(product))));
            } catch (JSONException e) {
                e.printStackTrace();
            }

            return convertView;
        }
    }
}
