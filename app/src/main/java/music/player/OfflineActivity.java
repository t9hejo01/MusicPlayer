package music.player;

import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class OfflineActivity extends AppCompatActivity {
    public static final String TAG = "offline";
    ConstraintLayout constraintLayout;
    MediaAdapterSwipe adapter;
    ListView listView;
    RecyclerView rvTrackList;
    TextView tvTotalSongs;
    ArrayList<Audio> audioList;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_offline);

        listView = findViewById(R.id.lvTrackList1);
        constraintLayout = findViewById(R.id.cLayout1);
        Log.d(TAG, "onCreate: Before loadaudio");
        loadAudio();
        adapter = new MediaAdapterSwipe(OfflineActivity.this, audioList);

        Log.d(TAG, "onCreate: after loadaudio");
        Collections.sort(audioList, new Comparator<Audio>() {
            @Override
            public int compare(Audio audio1, Audio audio2) {
                return audio1.getTitle().compareToIgnoreCase(audio2.getTitle());
            }
        });

        Log.d(TAG, "Before rv");
        listView.setAdapter(adapter);
        adapter.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void itemClick(int itemId, View view) {
                adapter.closeItem(itemId);
                switch (view.getId()) {
                    case R.id.cLayout1:
                        adapter.closeAllItems();
                        Log.d(TAG, "OnCreate: insideonClick");
                        Intent i = new Intent(OfflineActivity.this, MediaActivity.class);
                        i.putExtra("songId", itemId);
                        i.putExtra("audioList", audioList);
                        startActivity(i);
                        break;
                    case R.id.del:
                        Toast.makeText(OfflineActivity.this, "del", Toast.LENGTH_SHORT).show();
                        break;
                    case R.id.fav:
                        Toast.makeText(OfflineActivity.this, "fav", Toast.LENGTH_SHORT).show();
                        break;
                }
            }
        });

        tvTotalSongs = (TextView) findViewById(R.id.tvTotalSongs);
        tvTotalSongs.setText("Total Songs: " + audioList.size());

    }



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
}


