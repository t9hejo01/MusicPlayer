package music.player;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GestureDetectorCompat;


import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

import co.mobiwise.library.InteractivePlayerView;
import co.mobiwise.library.OnActionClickedListener;

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
    private final ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            LocalBinder binder = (LocalBinder) service;
            player = binder.getService();
            serviceBound = true;

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
        audioList = (ArrayList<Audio>) getIntent().getSerializableExtra("audioList");
        songIdReceived = getIntent().getIntExtra("songId", 0);

        ((TextView) findViewById(R.id.musicArtistName)).setText(audioList.get(songIdReceived).getArtist());
        ((TextView) findViewById(R.id.musicTitle)).setText(audioList.get(songIdReceived).getTitle());

        ((TextView) findViewById(R.id.tvNextSong1Title)).setText(audioList.get((songIdReceived + 1) % audioList.size()).getTitle());
        ((TextView) findViewById(R.id.tvNextSong2Title)).setText(audioList.get((songIdReceived + 2) % audioList.size()).getTitle());
        ((TextView) findViewById(R.id.tvNextSong3Title)).setText(audioList.get((songIdReceived + 3) % audioList.size()).getTitle());
        ((TextView) findViewById(R.id.tvNextSong1Artist)).setText(audioList.get((songIdReceived + 1) % audioList.size()).getTitle());
        ((TextView) findViewById(R.id.tvNextSong2Artist)).setText(audioList.get((songIdReceived + 2) % audioList.size()).getTitle());
        ((TextView) findViewById(R.id.tvNextSong1Artist)).setText(audioList.get((songIdReceived + 3) % audioList.size()).getTitle());

        swipeRLayout = findViewById(R.id.songPlayerTopLayout);
        swipeImageView = findViewById(R.id.imageView);
        swipeImageView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return gestureDetectorCompat.onTouchEvent(event);
            }
        });

        gestureDetectorCompat = new GestureDetectorCompat(this, this);

        Log.d("poo", "1");
        final InteractivePlayerView mInteractivePlayerView = findViewById(R.id.interactivePlayerView);
        long duration = audioList.get(songIdReceived).getDuration();
        mInteractivePlayerView.setMax(250);
        mInteractivePlayerView.setProgress(0);
        mInteractivePlayerView.setOnActionClickedListener(this);

        final ImageView imageView = findViewById(R.id.control);
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!mInteractivePlayerView.isPlaying()) {
                    playAudio(songIdReceived);
                    mInteractivePlayerView.start();
                    imageView.setBackgroundResource(R.drawable.ic_action_pause);
                } else {
                    mInteractivePlayerView.stop();
                    imageView.setBackgroundResource(R.drawable.ic_action_play);
                }
            }
        });

        shakeListener = new ShakeListener(MediaActivity.this);
        shakeListener.setOnShakeListener(new ShakeListener.OnShakeListener() {
            @Override
            public void onShake() {
                Log.d("poo", "onShake");
                stopServiceIfBound();
                playSong(songIdReceived + 1);
                finish();
            }
        });
    }

    private void playSong(int playIndex) {
        Intent intent = new Intent(MediaActivity.this, MediaActivity.class);
        intent.putExtra("songId", (audioList.size() + playIndex) % audioList.size());
        intent.putExtra("audioList", audioList);
        startActivity(intent);
    }

    private void stopServiceIfBound() {
        if (serviceBound) {
            Intent stopservice = new Intent(MediaActivity.this, MediaPlayerService.class);
            stopService(stopservice);
        }
    }

    @Override
    public boolean onDown(MotionEvent motionEvent) {
        return true;
    }

    @Override
    public void onShowPress(MotionEvent motionEvent) {

    }

    @Override
    public boolean onSingleTapUp(MotionEvent motionEvent) {
        return false;
    }

    @Override
    public boolean onScroll(MotionEvent motionEvent, MotionEvent motionEvent1, float v, float v1) {
        return true;
    }

    @Override
    public void onLongPress(MotionEvent motionEvent) {

    }

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        float diffY = e2.getY() - e1.getY();
        float diffX = e2.getX() - e1.getX();
        if (Math.abs(diffX) > Math.abs(diffY)) {
            if (Math.abs(diffX) > 100 && Math.abs(velocityX) > 100) {
                if (diffX > 0) {
                    Toast.makeText(this,"right", Toast.LENGTH_SHORT).show();
                    stopServiceIfBound();
                    playSong(songIdReceived - 1);
                    finish();
                } else {
                    Toast.makeText(this,"left", Toast.LENGTH_SHORT).show();
                    stopServiceIfBound();
                    playSong(songIdReceived - 1);
                    finish();
                }
            }
        } else {
            if (Math.abs(diffY) > 100 && Math.abs(velocityY) > 100) {
                if (diffY > 0) {
                    Toast.makeText(this, "down", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "up", Toast.LENGTH_SHORT).show();
                }
            }
        }
        return true;
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d("poo", "4");
        shakeListener.pause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d("poo", "5");
        shakeListener.resume();
    }

    @Override
    public void onActionClicked(int id) {
        switch (id) {
            case 1:
                break;
            case 2:
                break;
            case 3:
                break;
            default:
                break;
        }
    }

    private void playAudio(int audioIndex) {
        // Check if service is active
        if (!serviceBound) {
            // Store Serializable audioList to SharedPreferences
            StorageUtil storageUtil = new StorageUtil(getApplicationContext());
            storageUtil.storeAudio(audioList);
            storageUtil.storeAudioIndex(audioIndex);

            Intent playerIntent = new Intent(this, MediaPlayerService.class);
            startActivity(playerIntent);
            bindService(playerIntent, serviceConnection, Context.BIND_AUTO_CREATE);
        } else {
            // Store new audioIndex to SharedPreferences
            StorageUtil storageUtil = new StorageUtil(getApplicationContext());
            storageUtil.storeAudioIndex(audioIndex);

            // Service is active
            // Send broadcast to te service -> Play new audio
            Intent broadcastIntent = new Intent(Broadcast_PLAY_NEW_AUDIO);
            sendBroadcast(broadcastIntent);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        savedInstanceState.putBoolean("ServiceState", serviceBound);
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        serviceBound = savedInstanceState.getBoolean("serviceState");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (serviceBound) {
            unbindService(serviceConnection);
            // Service is active
            player.stopSelf();
        }
    }
}