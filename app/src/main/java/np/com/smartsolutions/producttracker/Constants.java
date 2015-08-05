package np.com.smartsolutions.producttracker;


public final class Constants {

    // Connection
    //public static final String URL = "http://172.16.31.236/test/tracker.php";
    public static final String URL = "http://smartsolutions.com.np/dev/prodtrak/tracker.php";
    //public static final String URL = "http://192.168.0.110/tracker.php";
    //public static final String URL = "http://192.168.43.34/tracker.php";

    // Server POST Query Constants
    public static final String GET_ENTRIES = "get_entries";
    public static final String DATE1 = "date1";
    public static final String DATE2 = "date2";
    public static final String SEND_ENTRY = "daily_update";
    public static final String ENTRY_ADDED = "ENTRY_ADDED";
    public static final String ADD_PRODUCT = "add_product";
    public static final String JSON_CLASS_PRODUCTS = "products";
    public static final String JSON_CLASS_ENTRIES = "entries";
    public static final String USER_OP = "user_operation";
    public static final String MAKE_ADMIN = "make_admin";
    public static final String ERROR = "ERROR";
    public static final String[] ADDITIONAL_COLUMNS = {"user_id", "date", "edited_time", "total"};


    // SavedPreferences Constants
    public static final String COLUMN_PREFS = "column_prefs";
    public static final String NUM_PRODUCTS = "number_of_products";
    public static final String GRAPH_PREFS = "graph_prefs";
    public static final String GRAPH_VIEW = "graph_prefs";
    public static final int YEAR = 1001;
    public static final int MONTH = 1002;
    public static final int WEEK = 1003;
    public static final int ALL = 1004;

    // Notifications
    public static final String SENT_TOKEN_TO_SERVER = "sent_token";
    public static final String REGISTRATION_COMPLETE = "reg_complete";

    // Calendar
    public static final String[] convertMonth = {
            "", "JAN", "FEB", "MAR", "APR", "MAY", "JUN", "JUL", "AUG", "SEP", "OCT", "NOV", "DEC"};
}
