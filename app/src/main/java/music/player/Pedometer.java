package music.player;

import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class Pedometer extends AppCompatActivity {
    DatabaseReference users;
    String token;
    FirebaseDatabase db;
    private SharedPreferences sharedPreferences;
    private Boolean started;
    private Float initSteps;
    private TextView tvSteps;
    Float curSteps = (float) 0;
    private TextView tvDistance;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pedometer);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        token = FirebaseInstanceId.getInstance().getToken();
        db = FirebaseDatabase.getInstance();
        users = db.getReference();

        tvSteps = findViewById(R.id.steps);
        ToggleButton btnPedo = findViewById(R.id.btnPedo);
        tvDistance = findViewById(R.id.distance);
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(Pedometer.this);
        started = sharedPreferences.getBoolean("running", false);
        initSteps = sharedPreferences.getFloat("steps", 0);

        if (started) {
            btnPedo.setChecked(true);
        }
        btnPedo.setOnCheckedChangeListener((buttonView, isChecked) -> {
            SharedPreferences.Editor editor;
            editor = sharedPreferences.edit();
            if (isChecked) {
                started = true;
                boolean alreadyStarted;
                alreadyStarted = sharedPreferences.getBoolean("running", false);

                if (!alreadyStarted) {
                    if (curSteps != 0) {
                        initSteps = curSteps;
                        editor.putFloat("steps", initSteps);
                        editor.putBoolean("running", true);
                        editor.apply();
                    }
                }
            } else {
                Calendar calendar = Calendar.getInstance();
                SimpleDateFormat sdf = (SimpleDateFormat) DateFormat.getDateTimeInstance();
                String date = sdf.format(calendar.getTime());
                String mid = getIntent().getStringExtra("uid");
                JSONObject jsonObject = new JSONObject();
                try {
                    String distance = tvDistance.getText().toString();
                    String steps = tvSteps.getText().toString();
                    jsonObject.put("distance", distance);
                    jsonObject.put("steps", steps);
                    jsonObject.put("date", date);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                if (mid != null) {
                    users.child("users").child(mid).push().setValue(jsonObject.toString());
                    Toast.makeText(Pedometer.this, "Updated to firebase", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(Pedometer.this, "uid still null", Toast.LENGTH_SHORT).show();
                }
            }
        });

        SensorManager sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        Sensor pedoMeter = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);
        Sensor stepDetect = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR);
        if (pedoMeter == null) {
            final AlertDialog alertDialog = new AlertDialog.Builder(Pedometer.this)
                    .setTitle("Step Counter")
                    .setMessage("Device doesn't support pedometer")
                    .setOnDismissListener(dialog -> finish())
                    .setNeutralButton("OK", (dialog, which) -> finish())
                    .show();
        } else {
            sensorManager.registerListener(new SensorEventListener() {
                @Override
                public void onSensorChanged(SensorEvent event) {
                    if (started) {
                        if (initSteps == 0) {
                            initSteps = event.values[0];
                            SharedPreferences.Editor editor = sharedPreferences.edit();
                            editor.putFloat("steps", initSteps);
                            editor.putBoolean("running", true);
                            editor.apply();
                        }

                        Float steps = event.values[0] - initSteps;
                        tvDistance.setText(distance(steps));
                        tvSteps.setText(String.format("%s", steps));
                    }
                    curSteps = event.values[0];
                }

                @Override
                public void onAccuracyChanged(Sensor sensor, int accuracy) {

                }
            }, pedoMeter, 100000);
        }

        if (stepDetect == null) {
            final AlertDialog alertDialog1 = new AlertDialog.Builder(Pedometer.this)
                    .setTitle("Step Counter")
                    .setMessage("Device doesn't support pedometer")
                    .setOnDismissListener(dialog -> finish())
                    .setNeutralButton("OK", (dialog, which) -> finish())
                    .show();
        } else {
            sensorManager.registerListener(new SensorEventListener() {
                @Override
                public void onSensorChanged(SensorEvent event) {

                }

                @Override
                public void onAccuracyChanged(Sensor sensor, int accuracy) {

                }
            }, stepDetect, 1000);
        }

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(v -> {
            Snackbar.make(v, "Replace with your own action", Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show();
            Intent i = new Intent(getApplicationContext(), TrackPedo.class);
            if (getIntent().getStringExtra("uid") != null) {
                i.putExtra("uid", getIntent().getStringExtra("uid"));
            } else {
                Toast.makeText(getApplicationContext(), "null uid", Toast.LENGTH_SHORT).show();
            }
            startActivity(i);
        });
    }

    public String distance(Float steps) {
        String result;

        float d = (steps * 25);
        int km = (int)(d / 100000);
        int m = (int)(d/100);
        if (km > 0) {
            result = d + " km";
        } else if (m > 0) {
            result = d + " m";
        } else {
            result = d + " cm";
        }
        return result;
    }
}