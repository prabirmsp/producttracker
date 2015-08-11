package np.com.smartsolutions.producttracker;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.jjoe64.graphview.DefaultLabelFormatter;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.text.DateFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;


public class EntriesRecyclerViewAdapter extends RecyclerView.Adapter<EntriesRecyclerViewAdapter.ViewHolder> {

    private static final String TAG = EntriesRecyclerViewAdapter.class.getSimpleName();
    public static final String KEY_ENTRY_JSON = "entry_json";
    public static final int VIEW_ENTRY = 0;
    public static final int VIEW_GRAPH = 1;
    ArrayList<EntriesRecyclerItem> mEntries;
    Context mContext;

    public EntriesRecyclerViewAdapter(Context context) {
        mContext = context;
        mEntries = new ArrayList<>();
    }

    public void updateEntries(ArrayList<EntriesRecyclerItem> entries) {
        mEntries = entries;
        notifyDataSetChanged();
    }

    @Override
    public int getItemViewType(int position) {
        return mEntries.get(position).viewType;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        ViewHolder holder = null;
        View view;
        switch (viewType) {
            case VIEW_ENTRY:
                view = View.inflate(mContext, R.layout.list_item, null);
                holder = new ViewHolder(view, viewType);
                break;
            case VIEW_GRAPH:
                view = View.inflate(mContext, R.layout.item_graph_view, null);
                holder = new ViewHolder(view, viewType);
                break;
            default:
                Log.e(TAG, "Unsupported viewType: " + viewType);
        }
        return holder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        switch (holder.viewType) {
            case VIEW_ENTRY:
                onBindEntry(holder, position);
                break;
            case VIEW_GRAPH:
                onBindGraph(holder, position);
                break;
        }
    }

    private void onBindGraph(ViewHolder holder, int position) {
        holder.graphView.removeAllSeries();
        if (mEntries.get(position).dataPointsMap != null) {
            int primary = mContext.getResources().getColor(R.color.primary);
            int primaryDark = mContext.getResources().getColor(R.color.primary_dark);
            int red = mContext.getResources().getColor(R.color.text_red);

            // Add Orders Graph
            DataPoint[] ordersPoints = mEntries.get(position).dataPointsMap.get(Constants.JSON_PROD_ORDERS);
            LineGraphSeries<DataPoint> orderSeries = new LineGraphSeries<>(ordersPoints);
            orderSeries.setColor(primary);
            holder.graphView.addSeries(orderSeries);

            // Add Returns Graph
            DataPoint[] returnsPoints = mEntries.get(position).dataPointsMap.get(Constants.JSON_PROD_RETURNS);
            LineGraphSeries<DataPoint> returnSeries = new LineGraphSeries<>(returnsPoints);
            returnSeries.setColor(red);
            holder.graphView.addSeries(returnSeries);


            // set manual x bounds
            SharedPreferences graphPrefs = mContext.getSharedPreferences(Constants.GRAPH_PREFS, 0);
            double minX;
            String viewStr = "";
            int viewPref = graphPrefs.getInt(Constants.GRAPH_VIEW, Constants.MONTH);
            if (viewPref == Constants.ALL) {
                minX = ordersPoints[0].getX();
                viewStr = "All";
            } else {
                Calendar min = Calendar.getInstance();
                switch (viewPref) {
                    case Constants.YEAR:
                        min.add(Calendar.YEAR, -1);
                        viewStr = "Year";
                        break;
                    case Constants.WEEK:
                        min.add(Calendar.HOUR, -168); // 168 hours in a week
                        viewStr = "Week";
                        break;
                    case Constants.MONTH:
                    default:
                        min.add(Calendar.MONTH, -1);
                        viewStr = "Month";
                        break;
                }
                minX = min.getTimeInMillis();
            }
            holder.graphView.getViewport().setMinX(minX);
            holder.graphView.getViewport().setMaxX(Calendar.getInstance().getTimeInMillis());
            holder.graphView.getViewport().setXAxisBoundsManual(true);
            holder.graphView.setTitle("Total Products (" + viewStr + ")");
            holder.graphView.setTitleColor(primaryDark);

            // set date label formatter
            final DateFormat labelFormat = new SimpleDateFormat("MMM dd");
            //holder.graphView.getGridLabelRenderer().setVerticalAxisTitle("Total");
            holder.graphView.getGridLabelRenderer().setLabelFormatter(
                    new DefaultLabelFormatter() {
                        @Override
                        public String formatLabel(double value, boolean isValueX) {
                            if (isValueX) {
                                // show normal x values
                                Date date = new Date((long) value);
                                return labelFormat.format(date);
                            } else {
                                // show currency for y values
                                if (value > 999)
                                    return coolFormat(value, 0);
                                else return "" + (int) value;
                            }
                        }
                    });

            holder.graphView.getGridLabelRenderer().setHorizontalLabelsColor(primaryDark);
            holder.graphView.getGridLabelRenderer().setVerticalLabelsColor(primaryDark);
            holder.graphView.getGridLabelRenderer().setNumHorizontalLabels(3); // only 4 because of the space
            holder.loading.setVisibility(View.INVISIBLE);
        }
        else { // Graph data not yet loaded
            holder.loading.setVisibility(View.VISIBLE);
        }

    }

    private void onBindEntry(ViewHolder holder, int position) {
        try {
            final Entry entry = mEntries.get(position).entry;
            DateFormat serverFormat = new SimpleDateFormat("yyyy-MM-dd");
            NumberFormat numberFormat = NumberFormat.getIntegerInstance();

            if (entry.hasOrders) {
                Date date = serverFormat.parse(entry.getFromOrder("date"));
                holder.month.setText(new SimpleDateFormat("MMM").format(date));
                holder.day.setText(new SimpleDateFormat("dd").format(date));
                holder.user.setText(entry.getFromOrder("user_id"));
                holder.editDate.setText("On " + entry.getFromOrder("edited_time"));
                holder.order.setText(numberFormat.format(Integer.parseInt(entry.getFromOrder("total"))));
                if (entry.hasReturns) {
                    holder.returns.setText(numberFormat.format(Integer.parseInt(entry.getFromReturn("total"))));
                    holder.label.setText("ORDERS / RETURNS");
                } else {
                    holder.returns.setText("");
                    holder.label.setText("ORDERS");
                }
            } else {
                Date date = serverFormat.parse(entry.getFromReturn("date"));
                holder.month.setText(new SimpleDateFormat("MMM").format(date));
                holder.day.setText(new SimpleDateFormat("dd").format(date));
                holder.user.setText(entry.getFromReturn("user_id"));
                holder.editDate.setText("On " + entry.getFromReturn("edited_time"));
                holder.returns.setText(numberFormat.format(Integer.parseInt(entry.getFromReturn("total"))));
                holder.order.setText("");
                holder.label.setText("RETURNS");
            }

            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(mContext, EntryInfoActivity.class);
                    intent.putExtra(KEY_ENTRY_JSON, entry.getJson());
                    mContext.startActivity(intent);
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public int getItemCount() {
        return mEntries.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        int viewType;
        View itemView;
        TextView month;
        TextView day;
        TextView user;
        TextView editDate;
        TextView label;
        TextView order;
        TextView returns;
        View loading;
        GraphView graphView;

        public ViewHolder(View itemView, int viewType) {
            super(itemView);
            this.viewType = viewType;
            this.itemView = itemView;
            switch (viewType) {
                case VIEW_ENTRY:
                    month = (TextView) itemView.findViewById(R.id.tv_month);
                    day = (TextView) itemView.findViewById(R.id.tv_day);
                    user = (TextView) itemView.findViewById(R.id.tv_user);
                    editDate = (TextView) itemView.findViewById(R.id.tv_edit_date);
                    order = (TextView) itemView.findViewById(R.id.tv_order);
                    returns = (TextView) itemView.findViewById(R.id.tv_return);
                    //label = (TextView) itemView.findViewById(R.id.tv_label);
                    label = new TextView(mContext);

                    break;
                case VIEW_GRAPH:
                    graphView = (GraphView) itemView.findViewById(R.id.graph_view);
                    loading = itemView.findViewById(R.id.tv_loading);
                    break;
                default:
                    Log.e(TAG, "Unsupported viewType: " + viewType);
            }
        }
    }

    public static class EntriesRecyclerItem {
        int viewType;
        Entry entry;
        HashMap<String, DataPoint[]> dataPointsMap;

        public EntriesRecyclerItem(Entry entry) {
            viewType = VIEW_ENTRY;
            this.entry = entry;
        }

        public EntriesRecyclerItem(int viewType) {
            this.viewType = viewType;
        }

        public boolean addDataPoints(HashMap<String, DataPoint[]> dataPointsMap) {
            boolean val;
            if (val = this.viewType == VIEW_GRAPH)
                this.dataPointsMap = dataPointsMap;
            return val;
        }

        public EntriesRecyclerItem(HashMap<String, DataPoint[]> dataPointsMap) {
            viewType = VIEW_GRAPH;
            this.dataPointsMap = dataPointsMap;

        }
    }

    private static char[] c = new char[]{'k', 'm', 'b', 't'};

    /**
     * Recursive implementation, invokes itself for each factor of a thousand, increasing the class on each invokation.
     *
     * @param n         the number to format
     * @param iteration in fact this is the class from the array c
     * @return a String representing the number n formatted in a cool looking way.
     */
    private static String coolFormat(double n, int iteration) {
        double d = ((long) n / 100) / 10.0;
        boolean isRound = (d * 10) % 10 == 0;//true if the decimal part is equal to 0 (then it's trimmed anyway)
        return (d < 1000 ? //this determines the class, i.e. 'k', 'm' etc
                ((d > 99.9 || isRound || (!isRound && d > 9.99) ? //this decides whether to trim the decimals
                        (int) d * 10 / 10 : d + "" // (int) d * 10 / 10 drops the decimal
                ) + "" + c[iteration])
                : coolFormat(d, iteration + 1));

    }
}
