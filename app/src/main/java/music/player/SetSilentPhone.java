package music.player;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class SetSilentPhone extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_silent_phone);
        ((Button)findViewById(R.id.start)).setOnClickListener(v -> {
            Intent i = new Intent(getApplicationContext(), SilentPhone.class);
            startService(i);
        });
        ((Button)findViewById(R.id.stop)).setOnClickListener(v -> {
            Intent i = new Intent(getApplicationContext(), SilentPhone.class);
            stopService(i);
        });
    }
}