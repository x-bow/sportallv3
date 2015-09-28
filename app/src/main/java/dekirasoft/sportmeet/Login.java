package dekirasoft.sportmeet;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.parse.LogInCallback;
import com.parse.ParseUser;
import com.parse.ParseException;

/**
 * Created by prateekarora on 28/09/15.
 */
public class Login extends Activity implements View.OnClickListener {

    EditText email, password;
    TextView login;
    ProgressDialog mProgressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.email_sign_in);

        /* initialize the class resources*/
        initView();
    }

    void initView() {
        email = (EditText) findViewById(R.id.email);
        password = (EditText) findViewById(R.id.password);
        login = (TextView) findViewById(R.id.login);
        login.setOnClickListener(this);
        mProgressBar = new ProgressDialog(this);

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
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.login:
                // Login user on Parse.com
                if (email.getText().toString().trim().length() == 0) {
                    email.setError("Please enter your email");
                    email.requestFocus();
                } else if (!ViewUtils.isValidEmail(email.getText().toString())) {
                    email.setError("Please enter valid email id.");
                    email.requestFocus();
                } else if (password.getText().toString().trim().length() == 0) {
                    password.setError("Please enter your password.");
                    password.requestFocus();
                } else {
                    userLogin(email.getText().toString(), password.getText().toString());
                }
                break;
        }
    }

    void userLogin(final String email, String password) {
        // Send data to Parse.com for verification

        mProgressBar.setMessage("Signing up...");
        mProgressBar.show();
        mProgressBar.setCancelable(false);

        ParseUser.logInInBackground(email, password,
                new LogInCallback() {
                    public void done(ParseUser user, ParseException e) {
                        if (user != null) {
                            // If user exist and authenticated, send user to Welcome.class
                            if (mProgressBar != null)
                                mProgressBar.dismiss();

                            Intent intent = new Intent(
                                    Login.this,
                                    NewUserSignUp.class);
                            intent.putExtra("name", "");
                            intent.putExtra("photoUrl", "");
                            intent.putExtra("email", email);
                            intent.putExtra("userNew", false);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(intent);
                            finish();
                            ViewUtils.showToast(getResources().getString(R.string.success_login), Login.this);
                        } else {
                            if (mProgressBar != null)
                                mProgressBar.dismiss();

                            ViewUtils.showErrorAlert(e.getMessage(), Login.this);
                        }
                    }
                });
    }
}
