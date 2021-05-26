package music.player;

import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.messaging.FirebaseMessaging;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

public class TrackPedo extends AppCompatActivity {
    RecyclerView rvPedo;
    TrackPedoAdapter adapter;
    DatabaseReference users;
    String token;
    String mid;
    FirebaseDatabase db;
    ArrayList<PedoUser> pedoUsers;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_track_pedo);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mid = getIntent().getStringExtra("uid");
        Toast.makeText(this, "in track pedo mid" + mid, Toast.LENGTH_SHORT).show();
        pedoUsers = new ArrayList<>();
        pedoUsers.add(new PedoUser("0", "00", "DD/MM/YYYY"));
        rvPedo = findViewById(R.id.rvPedo);
        rvPedo.setLayoutManager(new LinearLayoutManager(TrackPedo.this));

        adapter = new TrackPedoAdapter(getApplicationContext(), pedoUsers);

        token = String.valueOf(FirebaseMessaging.getInstance().getToken());
        db = FirebaseDatabase.getInstance();
        users = db.getReference();
        ChildEventListener cel = new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot snapshot, String previousChildName) {
                String p = snapshot.getValue(String.class);
                JSONObject object = null;
                try {
                    object = new JSONObject(p);
                } catch (JSONException e) {
                    e.printStackTrace();
                    Toast.makeText(getApplicationContext(), "invalid data type", Toast.LENGTH_SHORT).show();
                }
                try {
                    pedoUsers.add(new PedoUser(object.getString("steps"), object.getString("distance"), object.getString("date")));
                    adapter.notifyDataSetChanged();
                } catch (JSONException e) {
                    e.printStackTrace();
                    Toast.makeText(getApplicationContext(), "invalid data type 2", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onChildChanged(DataSnapshot snapshot, String previousChildName) {
                Log.d("tag", "onChildChanged");
            }

            @Override
            public void onChildRemoved(DataSnapshot snapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot snapshot, String previousChildName) {

            }

            @Override
            public void onCancelled(DatabaseError error) {

            }
        };
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd-MM-yyyy");
        String date = simpleDateFormat.format(calendar.getTime());

        JSONObject object = new JSONObject();
        try {
            object.put("distance", "100km");
            object.put("steps", "100");
            object.put("date", date);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        if (mid != null) {
            users.child("users").child(mid).getRef().addChildEventListener(cel);
            users.child("users").child(mid).push().setValue(object.toString());
        } else {
            Toast.makeText(this, "mid is still null", Toast.LENGTH_SHORT).show();
        }
    }
}