package np.com.smartsolutions.producttracker;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import java.util.ArrayList;

public class MakeAdminAdapter extends BaseAdapter {
    ArrayList<String> arrayList;
    Context context;
    ArrayList<Boolean> booleans;

    public MakeAdminAdapter(ArrayList<String> arrayList, Context context) {
        this.arrayList = arrayList;
        this.context = context;
        this.booleans = new ArrayList<>();
        for (int i = 0; i < arrayList.size(); i++) {
            booleans.add(false);
        }
    }

    public ArrayList<String> getEmailsToSend () {
        ArrayList<String> emails = new ArrayList<>();
        for (int i = 0; i < arrayList.size(); i++){
            if (booleans.get(i))
                emails.add(arrayList.get(i));
        }
        return emails;
    }

    @Override
    public int getCount() {
        return arrayList.size();
    }

    @Override
    public String getItem(int position) {
        return arrayList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = View.inflate(context, R.layout.list_item_add_admin, null);
        }
        ((TextView) convertView.findViewById(R.id.tv_email)).setText(getItem(position));
        CheckBox checkBox = (CheckBox) convertView.findViewById(R.id.checkbox);
        checkBox.setChecked(booleans.get(position));
        checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                booleans.set(position, isChecked);
            }
        });


        return convertView;
    }
}
