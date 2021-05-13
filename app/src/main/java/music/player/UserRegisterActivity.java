package music.player;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;

public class UserRegisterActivity extends AppCompatActivity {
    private final int checkIfUsernameTaken = 207;
    private final int addUser = 208;
    private final int[] fieldIDs = {
            R.id.usernameField,
            R.id.emailField,
            R.id.userPasswordField,
            R.id.userPasswordAgainField,
    };

    private final Utilities utilities = new Utilities();
    private TextInputLayout emailLayout, usernameLayout, passwordLayout, passwordAgainLayout;
    private TextView emailField, usernameField, passwordField, passwordAgainField;
    private Button registerButton;

    private FirebaseAuth mAuth;
    private String userEmail, username = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_register);

        mAuth = FirebaseAuth.getInstance();

        emailField = findViewById(R.id.emailField);
        usernameField = findViewById(R.id.usernameField);
        passwordField = findViewById(R.id.userPasswordField);
        passwordAgainField = findViewById(R.id.userPasswordAgainField);

        emailLayout = findViewById(R.id.emailLayout);
        usernameLayout = findViewById(R.id.usernameLayout);
        passwordLayout = findViewById(R.id.userPasswordLayout);
        passwordAgainLayout = findViewById(R.id.userPasswordAgainLayout);

        registerButton = findViewById(R.id.changePasswordButton);
        registerButton.setEnabled(false);
        addTextListeners();
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        setUserInterfaceEnabled(true);
        if (requestCode == checkIfUsernameTaken) {
            if (resultCode == RESULT_OK) {
                registerUser();
            } else {
                registerError("Username is taken");
            }
        } else if (requestCode == addUser) {
            if (resultCode == RESULT_OK) {
                registerComplete();
            } else {
                View contextView = this.findViewById(android.R.id.content);
                Snackbar snackbar = Snackbar
                        .make(contextView, R.string.registerProblem, Snackbar.LENGTH_LONG)
                        .setAction(R.string.tryAgain, v -> addUserToDatabase());
                snackbar.show();
            }
        }
    }

    private void registerComplete() {
        View contextView = this.findViewById(android.R.id.content);
        Snackbar snackbar = Snackbar
                .make(contextView, R.string.registerCompleteToast, Snackbar.LENGTH_INDEFINITE);
        snackbar.show();

        new CountDownTimer(5000, 1000) {
            int timeLeft = 5;

            @Override
            public void onTick(long millisUntilFinished) {
                int i = timeLeft--;
                snackbar.setText(R.string.registerCompleteToast + i + "seconds");
            }

            @Override
            public void onFinish() {
                int i = timeLeft--;
                snackbar.setText(R.string.registerCompleteToast + i + "seconds");
                snackbar.dismiss();

                Intent result = new Intent();
                setResult(RESULT_OK, result);
                finish(); // Destroy activity
            }
        }.start();
    }

    private void addTextListeners() {
        // Add text listeners to text fields
        for (int fieldID : fieldIDs) {
            ((TextView) findViewById(fieldID)).addTextChangedListener(generateTextListener(fieldID));
        }
    }

    private TextWatcher generateTextListener(int fieldName) {
        return new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @SuppressLint("NonConstantResourceId")
            @Override
            public void afterTextChanged(Editable s) {
                switch (fieldName) {
                    case R.id.usernameField:
                        utilities.checkUsernameField(getApplicationContext(), usernameLayout, usernameField, registerButton);
                        checkAllFields();
                        break;
                    case R.id.emailField:
                        utilities.checkEmailField(getApplicationContext(), emailLayout, emailField, registerButton);
                        checkAllFields();
                        break;
                    case R.id.userPasswordField:
                    case R.id.userPasswordAgainField:
                        checkPasswords();
                        checkAllFields();
                        break;
                }
            }
        };
    }

    private void setUserInterfaceEnabled(boolean isEnabled) {
    // Set layouts and button not disabled and disable
        // Used when starting registering so user can't press register button
        // and start register functions and
        // change also info during registering
        usernameLayout.setEnabled(isEnabled);
        emailLayout.setEnabled(isEnabled);
        passwordLayout.setEnabled(isEnabled);
        passwordAgainLayout.setEnabled(isEnabled);
        usernameLayout.setEnabled(isEnabled);
        registerButton.setEnabled(isEnabled);
    }

    private boolean checkPasswords() {
        return utilities.checkPasswords(getApplicationContext(), passwordLayout, passwordField, passwordAgainLayout, passwordAgainField);
    }

    private boolean checkAllFields() {
        if (utilities.checkEmailField(getApplicationContext(), emailLayout, emailField,registerButton) && utilities.checkUsernameField(getApplicationContext(), usernameLayout, usernameField, registerButton) && checkPasswords()) {
            registerButton.setEnabled(true);
            return true;
        }
        registerButton.setEnabled(false);
        return false;
    }

    private void addUserToDatabase() {
        Intent intent = new Intent(this, UserDatabase.class);
        Bundle extras = new Bundle();
        extras.putInt("requestCode", addUser);
        extras.putString("email", userEmail);
        extras.putString("username", userEmail);
        intent.putExtras(extras);
        startActivityForResult(intent, addUser);
    }

    private void registerUser() {
        String password = passwordField.getText().toString();

        mAuth.createUserWithEmailAndPassword(userEmail, password)
                .addOnCompleteListener(this, task -> {
                   if (task.isSuccessful()) {
                       FirebaseUser user = mAuth.getCurrentUser();
                       if (user != null) {
                           UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                                   .setDisplayName(username)
                                   .build();

                           user.updateProfile(profileUpdates)
                                   .addOnCompleteListener(update -> {
                                       if (update.isSuccessful()) {
                                           Log.d("", "User profile updated.");
                                       }
                                   });
                            addUserToDatabase();
                       }
                   } else {
                       Log.w("email signing", "signInWithEmail failed", task.getException());
                       String error = task.getException().getMessage();
                       registerError(error);
                   }
                });
    }

    public void startRegistering(View view) {
        // Disable fields and button
        setUserInterfaceEnabled(false);

        if (checkAllFields()) {
            userEmail = emailField.getText().toString();
            username = usernameField.getText().toString();

            Intent intent = new Intent(this, UserDatabase.class);
            Bundle extras = new Bundle();
            extras.putInt("requestCode", checkIfUsernameTaken);
            extras.putString("username", username);
            intent.putExtras(extras);
            startActivityForResult(intent, checkIfUsernameTaken);
        }
        setUserInterfaceEnabled(true);
    }

    private void registerError(String error) {
        final String emailTaken = "The email address is already taken.";
        final String usernameTaken = "Username already taken";

        View contextView = this.findViewById(android.R.id.content);
        Snackbar snackbar = null;
        switch (error) {
            case emailTaken:
                utilities.setErrorToLayout(getString(R.string.emailTaken), emailLayout);
                snackbar = Snackbar
                        .make(contextView, R.string.emailTaken, Snackbar.LENGTH_LONG)
                        .setAction(R.string.tryAgain, view -> startRegistering(contextView));
                break;

            case usernameTaken:
                utilities.setErrorToLayout(getString(R.string.usernameTaken), usernameLayout);
                snackbar = Snackbar
                        .make(contextView, R.string.usernameTaken, Snackbar.LENGTH_LONG)
                        .setAction(R.string.tryAgain, view -> startRegistering(contextView));
                break;
            default:
                Log.d("TAG", "registerError" + error);
        }

        snackbar.show();
    }
}