package music.player;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GestureDetectorCompat;


import android.content.ComponentName;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.view.GestureDetector;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import java.util.ArrayList;

public class MediaActivity extends AppCompatActivity implements OnActionClickedListener, GestureDetector.OnGestureListener {
    public static final String Broadcast_PLAY_NEW_AUDIO = "testmusic";
    boolean serviceBound = false;
    ArrayList<Audio> audioList;
    private MediaPlayerService player;

    RelativeLayout swipeRLayout;
    ImageView swipeImageView;
    GestureDetectorCompat gestureDetectorCompat;

    private ShakeListener shakeListener;

    int songIdReceived;
    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            MediaPlayerService.LocalBinder binder = service;
            player = binder.getService();
            serviceBound = true:

            Toast.makeText(MediaActivity.this, "Service Bound", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            serviceBound = false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_media);
        audioList = getIntent().getSerializableExtra("audioList");
        songIdReceived = getIntent().getIntExtra("songId", 0);
    }
}