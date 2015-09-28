package dekirasoft.sportmeet;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.parse.ParseUser;
import com.parse.SignUpCallback;

/**
 * Created by prateekarora on 27/09/15.
 */
public class SignUp extends Activity implements View.OnClickListener {

    EditText name, email, password, confirm_password;
    TextView free_account;
    ProgressDialog mProgressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sign_up);

        /* initialize the class resources*/
        initView();

        // emable sign up button click
        free_account.setOnClickListener(this);
    }

    void initView() {
        name = (EditText) findViewById(R.id.name);
        email = (EditText) findViewById(R.id.email);
        password = (EditText) findViewById(R.id.password);
        free_account = (TextView) findViewById(R.id.free_account);
        confirm_password = (EditText) findViewById(R.id.confirm_password);
        mProgressBar = new ProgressDialog(this);

        name.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                name.setError(null);
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        email.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                email.setError(null);
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        password.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                password.setError(null);
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        confirm_password.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                confirm_password.setError(null);
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
    }

    // Save new user data into Parse.com Data Storage
    void saveUser(String name, String email, String password) {
        mProgressBar.setMessage("Signing up...");
        mProgressBar.show();
        mProgressBar.setCancelable(false);
        ParseUser user = new ParseUser();
        user.setUsername(email);
        user.setEmail(email);
        user.setPassword(password);
        user.signUpInBackground(new SignUpCallback() {
            @Override
            public void done(com.parse.ParseException e) {
                if (e == null) {
                    // Show a simple Toast message upon successful registration
                    if (mProgressBar != null)
                        mProgressBar.dismiss();

                    // Clear all fields
                    clearFields();
                    ViewUtils.showSuccessAlert(getResources().getString(R.string.successful_signed_up), SignUp.this);
                } else {
                    if (mProgressBar != null)
                        mProgressBar.dismiss();

                    // Clear all fields
                    clearFields();
                    ViewUtils.showErrorAlert(e.getMessage(), SignUp.this);
                }
            }
        });
    }

    void clearFields() {
        name.setText("");
        email.setText("");
        password.setText("");
        confirm_password.setText("");
        name.requestFocus();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.free_account:
                if (name.getText().toString().trim().length() == 0) {
                    name.setError("Please enter your name");
                    name.requestFocus();
                } else if (email.getText().toString().trim().length() == 0) {
                    email.setError("Please enter your email");
                    email.requestFocus();
                } else if (!ViewUtils.isValidEmail(email.getText().toString())) {
                    email.setError("Please enter valid email id.");
                    email.requestFocus();
                } else if (password.getText().toString().trim().length() == 0) {
                    password.setError("Please enter your password.");
                    password.requestFocus();
                } else if (password.getText().toString().length() < 7) {
                    password.setError("Password length must be at least 7");
                    password.requestFocus();
                } else if (!(password.getText().toString().equalsIgnoreCase(confirm_password.getText().toString()))) {
                    password.setError("Password and Confirm Pasword does not match.");
                    confirm_password.setError("Password and Confirm Pasword does not match.");
                    password.requestFocus();
                } else
                    saveUser(name.getText().toString(), email.getText().toString(), password.getText().toString());
                break;
        }
    }
}
