package music.player;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.internal.OnConnectionFailedListener;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

public class LoginFirebase extends AppCompatActivity implements View.OnClickListener, OnConnectionFailedListener {
    private final int GOOGLE_SIGN_IN_CODE = 0123;
    EditText etEmail;
    EditText etPassword;
    AppCompatButton loginButton;
    Button googleSignInButton;

    FirebaseAuth mAuth;
    GoogleSignInClient mGoogleSignInClient;

    ProgressDialog progressDialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_firebase);
        etEmail = findViewById(R.id.email);
        etPassword = findViewById(R.id.password);
        loginButton = findViewById(R.id.loginButton);
        googleSignInButton = findViewById(R.id.loginGoogleButton);
        googleSignInButton.setOnClickListener((View.OnClickListener) this);
        loginButton.setOnClickListener((View.OnClickListener) this);

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        mAuth = FirebaseAuth.getInstance();
    }

    @Override
    public void onStop() {
        super.onStop();
        hideProgressDialog();
    }

    public void showProgressDialog() {
        if (progressDialog == null) {
            progressDialog = new ProgressDialog(this);
            progressDialog.setMessage(getString(R.string.loading));
            progressDialog.setIndeterminate(true);
        }

        progressDialog.show();
    }

    public void hideProgressDialog() {
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Toast.makeText(this, "connection failed", Toast.LENGTH_SHORT).show();
    }

    private void createAccount(String email, String password) {
        if (!validateForm()) {
            return;
        }

        showProgressDialog();
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(LoginFirebase.this, "Successfully created account", Toast.LENGTH_SHORT).show();
                            FirebaseUser user = mAuth.getCurrentUser();
                        } else {
                            Toast.makeText(LoginFirebase.this, "Authentication failed", Toast.LENGTH_SHORT).show();
                        }
                        hideProgressDialog();
                    }
                });
    }

    private void signIn(String email, String password) {
        if (!validateForm()) {
            return;
        }

        showProgressDialog();
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            FirebaseUser user = mAuth.getCurrentUser();
                            Toast.makeText(LoginFirebase.this, "Successfully signed in", Toast.LENGTH_SHORT).show();
                            String name = user.getDisplayName();
                            onSignIn(user.getUid(), name);
                        } else {
                            Toast.makeText(LoginFirebase.this, "Authentication failed", Toast.LENGTH_SHORT).show();
                        }
                        hideProgressDialog();
                    }
                });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode,data);
        if (requestCode == GOOGLE_SIGN_IN_CODE) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            if (result.isSuccess()) {
                GoogleSignInAccount account = result.getSignInAccount();
                firebaseAuthWithGoogle(account);
            } else {
                Toast.makeText(this, "on activity result failed", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        showProgressDialog();
        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mAuth.signInWithCredential(credential)
        .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    Toast.makeText(LoginFirebase.this, "Successfully signed in with Google", Toast.LENGTH_SHORT).show();
                    FirebaseUser user = mAuth.getCurrentUser();
                    String name = user.getDisplayName();
                    onSignIn(user.getUid(), name);
                } else {
                    Toast.makeText(LoginFirebase.this, "Authentication failed", Toast.LENGTH_SHORT).show();
                }
                hideProgressDialog();
            }
        });

    }

    private boolean validateForm() {
        boolean valid = true;
        return valid;
    }

    private void onSignIn(String uid, String name) {
        Intent signedIn = new Intent(LoginFirebase.this, OfflineActivityTry.class);
        signedIn.putExtra("uid", uid);
        signedIn.putExtra("username", name);
        startActivity(signedIn);
        finish();
    }

    private void googleSignIn() {
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleSignInClient.asGoogleApiClient());
        startActivityForResult(signInIntent, GOOGLE_SIGN_IN_CODE);
    }

    @Override
    public void onClick(View v) {
        int choice = v.getId();
        if (choice == R.id.loginButton) {
            signIn(etEmail.getText().toString(), etPassword.getText().toString());
        } else if (choice == R.id.loginGoogleButton) {
            googleSignIn();
        }
    }
}