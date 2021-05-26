package music.player;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.messaging.FirebaseMessaging;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Calendar;

public class Pedometer extends AppCompatActivity {
    DatabaseReference users;
    Task<String> token;
    FirebaseDatabase db;
    private SharedPreferences sharedPreferences;
    private Boolean started;
    private Float initSteps;
    private ToggleButton btnPedo;
    private TextView tvSteps;
    Float curSteps = Float.valueOf(0);
    private TextView tvDistance;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pedometer);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        db = FirebaseDatabase.getInstance();
        users = db.getReference();
        token = FirebaseMessaging.getInstance().getToken();

        tvSteps = findViewById(R.id.steps);
        btnPedo = findViewById(R.id.btnPedo);
        tvDistance = findViewById(R.id.distance);
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(Pedometer.this);
        started = sharedPreferences.getBoolean("running", false);
        initSteps = sharedPreferences.getFloat("steps", 0);


        if (started) {
            btnPedo.setChecked(true);
        }
        btnPedo.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                SharedPreferences.Editor editor = sharedPreferences.edit();
                if (isChecked) {
                    started = true;
                    Boolean alreadyStarted = sharedPreferences.getBoolean("running", false);

                    if (!alreadyStarted) {
                        if (curSteps != 0) {
                            initSteps = curSteps;
                            editor.putFloat("steps", initSteps);
                            editor.putBoolean("running", true);
                            editor.commit();
                        }
                    }
                } else {
                    Calendar calendar = Calendar.getInstance();
                    SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
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
            }
        });

        SensorManager sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        Sensor pedo = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);
        Sensor stepDetect = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR);
        if (pedo == null) {
            final AlertDialog alertDialog = new AlertDialog.Builder(Pedometer.this)
                    .setTitle("Step Counter")
                    .setMessage("Device doesn't support pedometer")
                    .setOnDismissListener(new DialogInterface.OnDismissListener() {
                        @Override
                        public void onDismiss(DialogInterface dialog) {
                            finish();
                        }
                    })
                    .setNeutralButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            finish();
                        }
                    })
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
                            editor.commit();
                        }

                        Float steps = event.values[0] - initSteps;
                        tvDistance.setText(distance(steps));
                        tvSteps.setText("" + steps);
                    }
                    curSteps = event.values[0];
                }

                @Override
                public void onAccuracyChanged(Sensor sensor, int accuracy) {

                }
            }, pedo, 100000);
        }

        if (stepDetect == null) {
            final AlertDialog alertDialog1 = new AlertDialog.Builder(Pedometer.this)
                    .setTitle("Step Counter")
                    .setMessage("Device doesn't support pedometer")
                    .setOnDismissListener(new DialogInterface.OnDismissListener() {
                        @Override
                        public void onDismiss(DialogInterface dialog) {
                            finish();
                        }
                    })
                    .setNeutralButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            finish();
                        }
                    })
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

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Snackbar.make(v, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
                Intent i = new Intent(getApplicationContext(), TrackPedo.class);
                if (getIntent().getStringExtra("uid") != null) {
                    i.putExtra("uid", getIntent().getStringExtra("uid"));
                } else {
                    Toast.makeText(getApplicationContext(), "null uid", Toast.LENGTH_SHORT).show();
                }
                startActivity(i);
            }
        });
    }

    public String distance(Float steps) {
        String result = "";

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