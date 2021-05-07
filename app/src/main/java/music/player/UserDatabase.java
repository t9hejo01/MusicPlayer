package music.player;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.HashMap;
import java.util.Map;

public class UserDatabase extends AppCompatActivity {
    private final int getSpecificUser = 200;
    private final int checkIfEmailTaken = 205;
    private final int checkIfUsernameTaken = 206;
    private final int addUser = 207;

    private String userEmail = "";
    private String username = "";
    private int requestCode = 0;

    FirebaseFirestore db = FirebaseFirestore.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle extras = getIntent().getExtras();
        requestCode = extras.getInt("requestCode");

        switch (requestCode) {
            case getSpecificUser:
                userEmail = extras.getString("email");
                getUser(userEmail);
                break;
            case checkIfUsernameTaken:
                username = extras.getString("username");
                checkIfUsernameTaken(username);
                break;

            case addUser:
                userEmail = extras.getString("email");
                username = extras.getString("username");
                insertUser(userEmail, username);
                break;
        }
    }

    private void getUser(String userEmail) {
        final String TAG = "get user from database";

        db.collection("users")
                .whereEqualTo("email", userEmail)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            String dbEmail = (String) document.get("email");
                            if (dbEmail != null && dbEmail.equals(userEmail)) {
                                String dbUsername = (String) document.get("username");
                                SharedPreferences sharedPreferences = getSharedPreferences("user", MODE_PRIVATE);
                                SharedPreferences.Editor editor = sharedPreferences.edit();
                                editor.putString("userid", document.getId());
                                editor.apply();
                                if (dbUsername != null) {
                                    returnUserLogin(dbUsername);
                                    return;
                                }
                                returnUserLogin("");
                                return;
                            }
                        }
                    } else {
                        Log.e("Error getting documents.", task.getException());
                    }
                });
    }

    private void insertUser(String userEmail, String username) {
        Map<String,Object> user = new HashMap<>();
        user.put("email", userEmail);
        user.put("username", username);

        String uid = db.collection("users").document().getId();
        SharedPreferences sharedPreferences = getSharedPreferences("user", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("userId", uid);
        editor.apply();

        db.collection("users").document(uid)
                .set(user)
                .addOnSuccessListener(documentReference -> returnUserRegistered(true))
                .addOnFailureListener(e -> returnUserRegistered(false));
    }

    private void checkIfEmailTaken(String userEmail) {
        db.collection("users")
                .whereEqualTo("email", userEmail)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document: task.getResult()) {
                            String dbEmail = (String) document.get("email");
                            if (dbEmail != null && dbEmail.equals(userEmail)) {
                                returnTaken(true);
                                return;
                            }
                        }
                    }
                    returnTaken(false);
                });

    }

    private void checkIfUsernameTaken(String username) {
        db.collection("users")
                .whereEqualTo("username", username)
                .get()
                .addOnCompleteListener(task -> {
                   if (task.isSuccessful()) {
                       for (QueryDocumentSnapshot document : task.getResult()) {
                           String dbUsername = (String) document.get("username");
                           if (dbUsername != null && dbUsername.equals(username)) {
                               returnTaken(true);
                               return;
                           }
                       }
                   }
                   returnTaken(false);
                });

    }

    private void returnTaken(boolean isTaken) {
        Intent result = new Intent();
        if (isTaken) {
            setResult(RESULT_CANCELED, result);
        } else {
            setResult(RESULT_OK, result);
        }
        finish();
    }

    private void returnUserRegistered(boolean success) {
        Intent result = new Intent();
        if (!success) {
            setResult(RESULT_CANCELED, result);
        } else {
            setResult(RESULT_OK, result);
        }
        finish();
    }

    public void returnUserLogin(String username) {
        Intent result = new Intent();
        if (!username.isEmpty()) {
            result.setData(Uri.parse(username));
            setResult(RESULT_OK, result);
        } else {
            setResult(RESULT_CANCELED, result);
        }
        finish();
    }


}