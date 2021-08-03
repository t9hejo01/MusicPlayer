package music.player;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import java.util.ArrayList;

import music.player.R.id;

public class OfflineActivity extends AppCompatActivity {
    public static final String TAG = "offline";
    ConstraintLayout constraintLayout;
    mediaAdapterSwipe adapter;
    ListView listView;
    TextView tvTotalSongs;
    ArrayList<Audio> audioList;


    @SuppressLint({"NonConstantResourceId", "DefaultLocale"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_offline);

        listView = findViewById(id.lvTrackList1);
        constraintLayout = findViewById(id.cLayout1);
        Log.d(TAG, "onCreate: Before loadaudio");
        //loadAudio();
        adapter = new mediaAdapterSwipe(OfflineActivity.this, audioList);

        Log.d(TAG, "onCreate: after loadaudio");
        audioList.sort((audio1, audio2) -> audio1.getTitle().compareToIgnoreCase(audio2.getTitle()));

        Log.d(TAG, "Before rv");
        listView.setAdapter(adapter);
        adapter.setOnItemClickListener((itemId, view) -> {
            adapter.closeItem(itemId);
            switch (view.getId()) {
                case id.cLayout1:
                    adapter.closeAllItems();
                    Log.d(TAG, "OnCreate: inside onClick");
                    Intent i = new Intent(OfflineActivity.this, MediaActivity.class);
                    i.putExtra("songId", itemId);
                    i.putExtra("audioList", audioList);
                    startActivity(i);
                    break;
                case id.del:
                    Toast.makeText(OfflineActivity.this, "del", Toast.LENGTH_SHORT).show();
                    break;
                case id.fav:
                    Toast.makeText(OfflineActivity.this, "fav", Toast.LENGTH_SHORT).show();
                    break;
            }
        });

        tvTotalSongs = findViewById(id.tvTotalSongs);
        tvTotalSongs.setText(String.format("Total Songs: %d", audioList.size()));

    }
/*
        private void loadAudio() {
            Log.d(TAG, "onCreate: inside loadaudio");
            ContentResolver resolver = getContentResolver();

            Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
            String selection = MediaStore.Audio.Media.IS_MUSIC + "!= 0";
            String sortOrder = MediaStore.Audio.Media.TITLE + "ASC";
            Cursor cursor = resolver.query(uri, null, selection, null, sortOrder);

            if (cursor != null && cursor.getCount() > 0) {
                audioList = new ArrayList<>();
                while (cursor.moveToNext()) {
                    String data = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATA));
                    String title = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.TITLE));
                    String album = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM));
                    String artist = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST));
                    long duration = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION));
                }
            }
            cursor.close();
    }
    */
}


