package np.com.smartsolutions.producttracker;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.EditText;

import java.util.HashMap;

public class SignUpActivity extends AppCompatActivity {

    public static final String TAG = SignUpActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        findViewById(R.id.button_cancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        findViewById(R.id.button_sign_up).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name, email, password;
                name = ((EditText) findViewById(R.id.name)).getText().toString().trim();
                email = ((EditText) findViewById(R.id.email)).getText().toString().trim();
                password = ((EditText) findViewById(R.id.password)).getText().toString();

                boolean proceed = false;
                // check fields
                if (name.equals("") || email.equals("") || password.equals(""))
                    alert("Please fill in all of the fields.");
                else if (!(email.contains("@") && email.contains(".")))
                    alert("Please enter a valid email address.");
                else if (password.length() < 8)
                    alert("The password must be at least 8 characters.");
                else
                    proceed = true;

                if (!proceed)
                    return;

                new SignUp().execute(name, email, password);
            }
        });
    }

    private void alert(String message) {
        new AlertDialog.Builder(this)
                .setTitle("Sign Up Error")
                .setMessage(message)
                .setPositiveButton("OK", null)
                .show();
    }


    public class SignUp extends AsyncTask<String, Void, Void> {
        ProgressDialog dialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            dialog = new ProgressDialog(SignUpActivity.this);
            dialog.setMessage("Signing up...");
            dialog.setCancelable(false);
            if (!ServiceHandler.isOnline(SignUpActivity.this))
                cancel(true);
            else {
                dialog.show();
            }

        }

        @Override
        protected Void doInBackground(String... params) {
            String response;
            // Get json from server
            try {
                HashMap<String, String> postParams = new HashMap<>();
                postParams.put(Constants.USER_OP, "register");
                postParams.put("name", params[0]);
                postParams.put("email", params[1]);
                postParams.put("password", params[2]);
                response = ServiceHandler.performPostCall(Constants.URL, postParams);

                Log.d(TAG, "Response: " + response);

                if (response.contains(Constants.ERROR))
                    cancel(true);
                else {
                    UserHandler user = new UserHandler(SignUpActivity.this);
                    user.loginFromJSON(response);
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
            DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Intent intent = new Intent(SignUpActivity.this, MainActivity.class);
                    startActivity(intent);
                    finish();
                }
            };
            new AlertDialog.Builder(SignUpActivity.this)
                    .setTitle("New User Created")
                    .setMessage("You have successfully registered your new account!")
                    .setPositiveButton("OK", listener)
                    .show();
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
            dialog.dismiss();
            new AlertDialog.Builder(SignUpActivity.this)
                    .setTitle("Error Logging In")
                    .setMessage("Please check your credentials and try again.")
                    .setPositiveButton("OK", null)
                    .show();
        }
    }
}
