package np.com.smartsolutions.producttracker;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.EditText;

import org.json.JSONObject;

import java.util.HashMap;

public class LoginActivity extends AppCompatActivity {

    public final static String TAG = LoginActivity.class.getSimpleName();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);

        UserHandler user = new UserHandler(this);

        if (user.isLoggedIn()) {
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
            finish();
        }

        setContentView(R.layout.activity_login);

        // Sign In
        findViewById(R.id.button_sign_in).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email, password;
                email = ((EditText) findViewById(R.id.email)).getText().toString();
                password = ((EditText) findViewById(R.id.password)).getText().toString();

                new SignIn().execute(email, password);
            }
        });

        // Sign Up
        findViewById(R.id.sign_up).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this, SignUpActivity.class);
                startActivity(intent);
            }
        });

    }

    public class SignIn extends AsyncTask<String, Void, Void> {
        ProgressDialog dialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            dialog = new ProgressDialog(LoginActivity.this);
            dialog.setMessage("Logging in...");
            dialog.setCancelable(false);
            if (!ServiceHandler.isOnline(LoginActivity.this))
                cancel(true);
            else
                dialog.show();
        }

        @Override
        protected Void doInBackground(String... params) {
            String response;
            // Get json from server
            try {
                HashMap<String, String> postParams = new HashMap<>();
                postParams.put(Constants.USER_OP, "login");
                postParams.put("email", params[0]);
                postParams.put("password", params[1]);
                response = ServiceHandler.performPostCall(Constants.URL, postParams);

                Log.d(TAG, "Response: " + response);

                JSONObject returnedObject = new JSONObject(response);
                if(returnedObject.getBoolean(Constants.SUCCESS)) {
                    // Success
                    UserHandler user = new UserHandler(LoginActivity.this);
                    user.loginFromJSON(returnedObject.getString(Constants.DATA));

                } else {
                    // Error
                    Log.d(TAG, "Error in response: " + returnedObject.getString(Constants.DATA));
                    cancel(true);
                }

            } catch (Exception e) {
                e.printStackTrace();
                this.cancel(true);
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            dialog.dismiss();
            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
            dialog.dismiss();
            new AlertDialog.Builder(LoginActivity.this)
                    .setTitle("Error Logging In")
                    .setMessage("Please check your credentials or connection and try again.")
                    .setPositiveButton("OK", null)
                    .show();
        }
    }
}
