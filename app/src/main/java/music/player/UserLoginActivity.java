package music.player;

import androidx.annotation.IntRange;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkCapabilities;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

import java.util.Objects;

public class UserLoginActivity extends AppCompatActivity {
    private final int loginGoogleCode = 201;
    private final int checkIfEmailTaken = 205;
    private final int verifyEmail = 202;

    private final String SHARED_PREF_USER = "user";
    private final String KEY_FIRST_GOOGLE_LOGIN = "googlelogin";

    private GoogleSignInClient mGoogleSignInClient;
    private FirebaseAuth mAuth;

    private EditText emailField, passwordField;
    private Button loginButton;
    private SharedPreferences sharedPreferences;
    private String email;

    private boolean emailSend = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_login);

        // Google Sign In configuration
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
        mAuth = FirebaseAuth.getInstance();

        sharedPreferences = getSharedPreferences(SHARED_PREF_USER, MODE_PRIVATE);

        emailField = findViewById(R.id.loginEmailField);
        passwordField = findViewById(R.id.loginUserPasswordField);
        loginButton = findViewById(R.id.loginButton);

        checkInternet();
        checkIfVerified();
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == checkIfEmailTaken) {
            Log.d("TAG", "onActivityResult: email taken" + resultCode);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            if (resultCode == RESULT_OK) {
                editor.putBoolean(KEY_FIRST_GOOGLE_LOGIN, true);
                editor.apply();
                sendEmail();
            } else {
                editor.putBoolean(KEY_FIRST_GOOGLE_LOGIN, false);
                editor.apply();
                loginSuccess();
            }
        } else if (requestCode == loginGoogleCode) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account.getIdToken());
            } catch (ApiException e) {
                Log.w("TAG", "Google Sign In failed", e);
            }
        } else if (requestCode == verifyEmail) {
            boolean emailVerified = Objects.requireNonNull(mAuth.getCurrentUser()).isEmailVerified();
            if (emailVerified) {
                loginSuccess();
            } else {
                View contextView = this.findViewById(android.R.id.content);
                Snackbar snackbar = Snackbar
                        .make(contextView, R.string.loginError, Snackbar.LENGTH_INDEFINITE)
                        .setAction(R.string.tryAgain, view -> googleSignIn(contextView));
                snackbar.show();
            }
        }
    }

    private void firebaseAuthWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                   View contextView = this.findViewById(android.R.id.content);
                   if (task.isSuccessful()) {
                       checkIfUserHasCreatedAccount();
                   } else {
                       Snackbar snackbar = Snackbar
                               .make(contextView, "Login failed.", Snackbar.LENGTH_INDEFINITE)
                               .setAction("Please try again", view -> googleSignIn(contextView));
                       snackbar.show();
                   }
                });
    }

    private void loginSuccess() {
        View contextView = this.findViewById(android.R.id.content);
        Snackbar snackbar = Snackbar
                .make(contextView, getString(R.string.loginCompleteToast), Snackbar.LENGTH_INDEFINITE);
        snackbar.show();

        new CountDownTimer(4000, 1000) {
            int timeLeft = 4;

            @Override
            public void onTick(long millisUntilFinished) {
                int i = timeLeft--;
                snackbar.setText(getString(R.string.loginCompleteToast) + i + "seconds");
            }

            @Override
            public void onFinish() {
                int i = timeLeft--;
                snackbar.setText(getString(R.string.loginCompleteToast) + i + "seconds");
                snackbar.dismiss();
                Intent result = new Intent();
                setResult(RESULT_OK, result);
                finish();
            }
        }.start();
    }

    private void checkIfUserHasCreatedAccount() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        assert user != null;
        email = user.getEmail();
        Intent intent = new Intent(this, UserDatabase.class);
        Bundle extras = new Bundle();
        extras.putInt("requestCode", checkIfEmailTaken);
        extras.putString("email", email);
        intent.putExtras(extras);
        startActivityForResult(intent, checkIfEmailTaken);
    }

    private void sendEmail() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (!emailSend && user != null) {
            user.sendEmailVerification()
                    .addOnCompleteListener(emailTask -> {
                        View contextView = this.findViewById(android.R.id.content);
                        if (emailTask.isSuccessful()) {
                            Snackbar snackbar = Snackbar
                                    .make(contextView, R.string.emailSend, Snackbar.LENGTH_INDEFINITE)
                                    .setAction(R.string.openEmailApp, view -> openEmailApp());
                            snackbar.show();
                            emailSend = true;
                            checkIfVerified();
                        } else {
                            Snackbar snackbar = Snackbar
                                    .make(contextView, R.string.emailSendFail, Snackbar.LENGTH_INDEFINITE)
                                    .setAction(R.string.tryAgain, view -> sendEmail());
                            snackbar.show();
                        }
                    });
        }
    }

    private void checkIfVerified() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            new CountDownTimer(600000, 1000) {
                boolean emailVerified = false;

                @Override
                public void onTick(long millisUntilFinished) {
                    emailVerified = user.isEmailVerified();
                    if (emailVerified) {
                        email = user.getEmail();
                        cancel();
                        loginSuccess();
                    }
                    Log.d("TAG", "onTick: not verified");
                }

                @Override
                public void onFinish() {
                    cancel();
                }
            }.start();
        }
    }

    private void openEmailApp() {
        Intent emailIntent = new Intent(Intent.ACTION_MAIN);
        startActivityForResult(emailIntent, verifyEmail);
    }

    public void googleSignIn(View view) {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, loginGoogleCode);
    }

    public void login(View view) {
        String inputEmail = emailField.getText().toString();
        String inputPassword = passwordField.getText().toString();

        if (!inputEmail.isEmpty() && !inputPassword.isEmpty()) {
            View contextView = this.findViewById(android.R.id.content);

            mAuth.signInWithEmailAndPassword(inputEmail, inputPassword)
                    .addOnCompleteListener(this, task -> {
                       if (task.isSuccessful()) {
                           FirebaseUser user = mAuth.getCurrentUser();
                           if (user != null) {
                               loginSuccess();
                           }
                       } else {
                           Log.w("email signin", "signInWithEmail:failure", task.getException());

                           Snackbar snackbar = Snackbar
                                   .make(contextView, R.string.loginError, Snackbar.LENGTH_INDEFINITE)
                                   .setAction(R.string.tryAgain, view1 -> login(contextView));
                           snackbar.show();
                       }
                    });
        }
    }

    public void register(View view) {
        Intent result = new Intent();
        result.setData(Uri.parse("Register"));
        setResult(RESULT_CANCELED, result);
        finish();
    }

    private void checkInternet() {
        View contextView = this.findViewById(android.R.id.content);

        if (getConnectionType(this) == 0) {
            loginButton.setEnabled(false);

            Snackbar snackbar = Snackbar
                    .make(contextView, "Internet connection is missing.", Snackbar.LENGTH_INDEFINITE)
                    .setAction("Please try again", view -> checkInternet());
            snackbar.show();
        } else {
            loginButton.setEnabled(true);

            Snackbar snackbar = Snackbar
                    .make(contextView, "You have now connection to Internet!", Snackbar.LENGTH_LONG);
            snackbar.setAction("Nice", view -> snackbar.dismiss());
            snackbar.show();
        }
    }

    @IntRange(from = 0, to = 3)
    private static int getConnectionType(Context context) {
        int result = 0;
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm != null) {
            NetworkCapabilities capabilities = cm.getNetworkCapabilities(cm.getActiveNetwork());
            if (capabilities != null) {
                if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)) {
                    result = 1;
                } else if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
                    result = 2;
                } else if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_VPN)) {
                    result = 3;
                }
            }
        }
        return result;
    }
}