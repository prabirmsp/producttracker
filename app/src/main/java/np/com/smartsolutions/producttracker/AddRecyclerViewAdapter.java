package np.com.smartsolutions.producttracker;

import android.content.Context;
import android.support.design.widget.TextInputLayout;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.EditText;

import java.util.ArrayList;
import java.util.Set;

/**
 * Created by prabir on 7/23/15.
 */
public class AddRecyclerViewAdapter extends RecyclerView.Adapter<AddRecyclerViewAdapter.ViewHolder> {
    ArrayList<String> mProducts;
    ArrayList<String> mEntries;
    Context mContext;

    public AddRecyclerViewAdapter(Context context, ArrayList<String> products) {
        mContext = context;
        mProducts = products;
        mEntries = new ArrayList<>();
        for(int i = 0; i < products.size(); i++)
            mEntries.add("");
    }

    public ArrayList<String> getEntries () {
        return mEntries;
    }

/*    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        final ViewHolder holder;
        if (convertView == null) {
            holder = new ViewHolder();
            holder.pos = position;
            convertView = View.inflate(mContext, R.layout.list_item_add, null);
            holder.editText = (EditText) convertView.findViewById(R.id.edit_text_product);
            holder.textInputLayout = (TextInputLayout) convertView.findViewById(R.id.text_input_product);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        holder.textInputLayout.setHint(getItem(holder.pos));
        holder.editText.setText(mEntries.get(holder.pos));
        holder.editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                mEntries.set(holder.pos, s.toString());
            }
        });

        convertView.setTag(holder);
        return convertView;
    }*/

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        LayoutInflater inflater = LayoutInflater.from(viewGroup.getContext());
        return new ViewHolder(inflater.inflate(R.layout.list_item_add, viewGroup, false));
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, final int i) {

        viewHolder.textInputLayout.setHint(mProducts.get(i));
        viewHolder.editText.setText(mEntries.get(i));
        viewHolder.editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                mEntries.set(i, s.toString());
            }
        });
    }

    @Override
    public int getItemCount() {
        return mProducts.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder{
        TextInputLayout textInputLayout;
        EditText editText;

        public ViewHolder(View itemView) {
            super(itemView);

            textInputLayout = (TextInputLayout) itemView.findViewById(R.id.text_input_product);
            editText = (EditText) itemView.findViewById(R.id.edit_text_product);

        }
    }
}
