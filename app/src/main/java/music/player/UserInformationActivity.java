package music.player;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;


import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

public class UserInformationActivity extends AppCompatActivity {
    private final int loginGoogleCode = 202;

    private int[] fieldIDs = {
            R.id.userPasswordField,
            R.id.userPasswordAgainField
    };

    private TextInputLayout passwordLayout, passwordAgainLayout;
    private TextView emailField, usernameField, passwordField, passwordAgainField;
    private TextView reauthEmailField, reauthPasswordField;
    private Button changePasswordButton, loginButton;

    private final Utilities utilities = new Utilities();

    private GoogleSignInClient mGoogleSignInClient;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_login);

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        mAuth = FirebaseAuth.getInstance();

        reauthEmailField = findViewById(R.id.loginEmailField);
        reauthPasswordField = findViewById(R.id.loginUserPasswordField);
        loginButton = findViewById(R.id.loginButton);

        View contextView = findViewById(android.R.id.content);
        Snackbar snackbar = Snackbar
                .make(contextView, R.string.reauthenticateToSeeInformation, Snackbar.LENGTH_LONG);
        snackbar.show();
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == loginGoogleCode) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account.getIdToken());
            } catch (ApiException e) {
                Log.w("TAG", "Google sign in failed", e);
            }
        }
    }

    public void emailLogin(View view) {
        String inputEmail = emailField.getText().toString();
        String inputPassword = passwordField.getText().toString();

        if (!inputEmail.isEmpty() && !inputPassword.isEmpty()) {
            View contextView = this.findViewById(android.R.id.content);

            mAuth.createUserWithEmailAndPassword(inputEmail, inputPassword)
                    .addOnCompleteListener(this, task -> {
                       if (task.isSuccessful()) {
                           FirebaseUser user = mAuth.getCurrentUser();
                           if (user != null) {
                               reAuthenticationSuccess();
                               return;
                           }
                       }

                       Snackbar snackbar = Snackbar
                                .make(contextView, R.string.loginError, Snackbar.LENGTH_INDEFINITE)
                                .setAction(R.string.tryAgain, view1 -> emailLogin(contextView));
                       snackbar.show();
                    });
        }
    }

    private void reAuthenticationSuccess() {
        setContentView(R.layout.activity_user_information);

        emailField = findViewById(R.id.emailField);
        usernameField = findViewById(R.id.usernameField);
        passwordField = findViewById(R.id.userPasswordField);
        passwordAgainField = findViewById(R.id.userPasswordAgainField);

        passwordLayout = findViewById(R.id.userPasswordLayout);
        passwordAgainLayout = findViewById(R.id.userPasswordAgainLayout);

        changePasswordButton = findViewById(R.id.changePasswordButton);

        addTextListeners();
        getInformation();
    }

    private void firebaseAuthWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    View contextView = this.findViewById(android.R.id.content);
                    if (task.isSuccessful()) {
                        reAuthenticationSuccess();
                    } else {
                        Snackbar snackbar = Snackbar
                                .make(contextView, "Login failed.", Snackbar.LENGTH_INDEFINITE)
                                .setAction("Please try again", view -> googleSignIn(contextView));
                        snackbar.show();
                    }
                });
    }

    public void googleSignIn(View view) {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, loginGoogleCode);
    }

    public void changePassword(View view) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (checkPasswords() && user != null) {
            String newPassword = passwordField.getText().toString();

            user.updatePassword(newPassword)
                    .addOnCompleteListener(task -> {
                       View contextView = this.findViewById(android.R.id.content);
                       if (task.isSuccessful()) {
                           Snackbar snackbar = Snackbar
                                   .make(contextView, R.string.passwordUpdateSuccess, Snackbar.LENGTH_LONG);
                           snackbar.show();

                           passwordField.setText("");
                           passwordAgainField.setText("");
                           return;
                       }
                       Log.w("password change", "password error", task.getException());
                       Snackbar snackbar = Snackbar
                               .make(contextView, R.string.passwordUpdateFailed, Snackbar.LENGTH_INDEFINITE)
                               .setAction(R.string.tryAgain, view1 -> changePassword(contextView));
                       snackbar.show();
                    });
        }
    }

    private void getInformation() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            String username = user.getDisplayName();
            String userEmail = user.getEmail();
            if (username != null && userEmail != null) {
                changePasswordButton.setEnabled(true);
                passwordLayout.setEnabled(true);
                passwordAgainLayout.setEnabled(true);

                emailField.setText(userEmail);
                usernameField.setText(username);
            } else {
                changePasswordButton.setEnabled(false);
                passwordLayout.setEnabled(false);
                passwordAgainLayout.setEnabled(false);

                View contextView = this.findViewById(android.R.id.content);
                Snackbar snackbar = Snackbar
                        .make(contextView, R.string.cantFindUserInformation, Snackbar.LENGTH_INDEFINITE)
                        .setAction(R.string.tryAgain, view -> getInformation());
                snackbar.show();
            }
        }
    }

    private void addTextListeners() {
        for (int fieldID : fieldIDs) {
            ((TextView) findViewById(fieldID)).addTextChangedListener(generateTextListener());
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
                checkPasswords();
            }
        };
    }

    private boolean checkPasswords() {
        return utilities.checkPasswords(getApplicationContext(), passwordLayout, passwordField, passwordAgainLayout, passwordAgainField);
    }
}