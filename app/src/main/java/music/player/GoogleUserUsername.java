package music.player;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;


public class GoogleUserUsername extends AppCompatActivity {
    private final int checkIfUsernameTaken = 207;
    private final int addUser = 208;

    private String username = "";
    private String email = "";

    private final String SHARED_PREF_USER = "user";
    private final String KEY_USERNAME = "username";
    private final String KEY_FIRSTGOOGLELOGIN = "googlelogin";

    private SharedPreferences sharedPreferences;
    private final Utilities utilities = new Utilities();

    private TextView usernameField;
    private TextInputLayout usernameLayout;
    private Button chooseUsernameButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_google_user_usernme);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        sharedPreferences = getSharedPreferences(SHARED_PREF_USER, MODE_PRIVATE);
        username = sharedPreferences.getString(KEY_USERNAME, "");

        if (username.isEmpty() && user != null) {
            username = user.getDisplayName();
            username = username != null ? username.replaceAll("\\s+", "") : "";
        }

        if (user != null) {
            email = user.getEmail();
        }

        chooseUsernameButton = findViewById(R.id.chooseUsernameButton);
        usernameLayout = findViewById(R.id.usernameLayout);
        usernameField = findViewById(R.id.usernameField);

        usernameField.setText(username);
        usernameField.addTextChangedListener(generateTextListener());
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        View contextView = this.findViewById(android.R.id.content);
        if (requestCode == checkIfUsernameTaken) {
            if (resultCode == RESULT_OK) {
                addUserToDatabase();
            } else {
                utilities.setErrorToLayout(getString(R.string.usernameTaken), usernameLayout);

                Snackbar snackbar = Snackbar
                        .make(contextView, R.string.usernameTaken, Snackbar.LENGTH_LONG)
                        .setAction(R.string.tryAgain, view -> updateUsername(contextView));
                snackbar.show();
            }
        } else if (requestCode == addUser) {
            if (resultCode == RESULT_OK) {
                updateGoogleUsername();
            } else {
                Snackbar snackbar = Snackbar
                        .make(contextView, R.string.registerProblem, Snackbar.LENGTH_LONG)
                        .setAction(R.string.tryAgain, v -> addUserToDatabase());
                snackbar.show();
            }
        }
    }

    private TextWatcher generateTextListener() {
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
                // Different errors is wanted to show depending on the field.
                String textFieldText = usernameField.getText().toString();
                if (textFieldText.isEmpty()) {
                    utilities.setErrorToLayout(getString(R.string.usernameEmpty), usernameLayout);
                    chooseUsernameButton.setEnabled(false);
                    return;
                } else if (textFieldText.contains(" ")) {
                    utilities.setErrorToLayout(getString(R.string.usernameNoSpaces), usernameLayout);
                    chooseUsernameButton.setEnabled(false);
                    return;
                }
                chooseUsernameButton.setEnabled(true);
                usernameLayout.setErrorEnabled(false);
            }
        };
    }

    private void addUserToDatabase() {
        Intent intent = new Intent(this, UserDatabase.class);
        Bundle extras = new Bundle();
        extras.putInt("requestCode", addUser);
        extras.putString("email", email);
        extras.putString("username", username);
        intent.putExtras(extras);
        startActivityForResult(intent, addUser);
    }

    public void updateUsername(View view) {
        // Check if inputted username is taken.
        username = usernameField.getText().toString();
        if (!username.isEmpty()) {
            Intent intent = new Intent(this, UserDatabase.class);
            Bundle extras = new Bundle();
            extras.putInt("requestCode", checkIfUsernameTaken);
            extras.putString("username", username);
            intent.putExtras(extras);
            startActivityForResult(intent, checkIfUsernameTaken);
        }
    }

    private void updateGoogleUsername() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if (!username.isEmpty() && user != null) {
            UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                    .setDisplayName(username)
                    .build();

            user.updateProfile(profileUpdates)
                    .addOnCompleteListener(task -> {
                       if (task.isSuccessful()) {
                           Log.d("TAG", "User profile updated.");
                       }
                    });

            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putBoolean(KEY_FIRSTGOOGLELOGIN, false);
            editor.apply();

            Intent result = new Intent();
            setResult(RESULT_OK, result);
            // Destroy activity and not exist in Back stack.
            finish();
        }
    }
}