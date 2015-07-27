package np.com.smartsolutions.producttracker;


import com.squareup.timessquare.CalendarPickerView;

public final class Constants {

    // Connection
    public static final String URL = "http://172.16.31.236/tracker.php";
    //public static final String URL = "http://192.168.0.110/tracker.php";

    // Server POST Query Constants
    public static final String GET_ENTRIES = "get_entries";
    public static final String DATE1 = "date1";
    public static final String DATE2 = "date2";
    public static final String SEND_ENTRY = "daily_update";
    public static final String ENTRY_ADDED = "ENTRY_ADDED";
    public static final String JSON_CLASS_PRODUCTS = "products";
    public static final String JSON_CLASS_ENTRIES = "entries";
    public static final String USER_OP = "user_operation";
    public static final String ERROR = "ERROR";


    // SavedPreferences Constants
    public static final String COLUMN_PREFS = "column_prefs";
    public static final String NUM_PRODUCTS = "number_of_products";

    // Notifications
    public static final String SENT_TOKEN_TO_SERVER = "sent_token";
    public static final String REGISTRATION_COMPLETE = "reg_complete";

    // Calendar
    public static final String[] convertMonth = {
            "", "JAN", "FEB", "MAR", "APR", "MAY", "JUN", "JUL", "AUG", "SEP", "OCT", "NOV", "DEC"};
}
