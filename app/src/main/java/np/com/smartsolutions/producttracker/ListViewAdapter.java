package np.com.smartsolutions.producttracker;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;


public class ListViewAdapter extends BaseAdapter {

    ArrayList<HashMap<String, String>> mEntries;
    Context mContext;

    public ListViewAdapter(Context context) {
        mContext = context;
        mEntries = new ArrayList<>();
    }

    public void updateEntries(ArrayList<HashMap<String, String>> entries) {
        mEntries = entries;
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return mEntries.size();
    }

    @Override
    public Object getItem(int position) {
        return mEntries.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) mContext
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.list_item, null);
        }
        TextView month = (TextView) convertView.findViewById(R.id.tv_month);
        TextView day = (TextView) convertView.findViewById(R.id.tv_day);
        TextView user = (TextView) convertView.findViewById(R.id.tv_user);
        TextView editDate = (TextView) convertView.findViewById(R.id.tv_edit_date);
        TextView total = (TextView) convertView.findViewById(R.id.tv_total);

        HashMap<String, String> entry = mEntries.get(position);
        String date = entry.get("date");
        month.setText(Constants.convertMonth[Integer.parseInt(date.substring(5, 7))]);
        day.setText(date.substring(8));
        user.setText(entry.get("user_id"));
        editDate.setText("On " + entry.get("edited_time"));
        total.setText(entry.get("total"));

        return convertView;
    }
}
