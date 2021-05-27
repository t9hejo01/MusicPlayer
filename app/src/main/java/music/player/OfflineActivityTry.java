package music.player;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class OfflineActivityTry extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    public static final String TAG = "offline";
    ConstraintLayout constraintLayout;
    MediaPlayerSwipe adapterSwipe;
    ListView listView;
    RecyclerView rvTrackList;
    MediaAdapter mediaAdapter;
    ArrayList<Audio> audioList;
    TextView tvTotalSongs;
    StorageReference storageReference;
    ProgressDialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_offline_try);
        if (getIntent().getStringExtra("uid") != null) {
            StorageUtil.uid = getIntent().getStringExtra("uid");
            Toast.makeText(getApplicationContext(), StorageUtil.uid, Toast.LENGTH_SHORT).show();
        }

        if (getIntent().getStringExtra("username") != null) {
            StorageUtil.username = getIntent().getStringExtra("username");
        }

        dialog = new ProgressDialog(this);
        storageReference = FirebaseStorage.getInstance().getReference();
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Snackbar.make(v, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        View header = navigationView.getHeaderView(0);
        TextView name = header.findViewById(R.id.username);
        name.setText(StorageUtil.username);

        listView = findViewById(R.id.lvTrackList);
        constraintLayout = findViewById(R.id.cLayout);

        Log.d(TAG, "onCreate:Before loadAudio");
        Long pid = getIntent().getLongExtra("playlistId", -1);
        if (pid == -1) {
            loadAudio(getApplicationContext());
        } else {
            audioList = PlayListHelper.getAllSongsOfPlaylist(OfflineActivityTry.this, pid);
        }

        if (audioList != null) {
            adapterSwipe = new MediaPlayerSwipe(OfflineActivityTry.this, audioList);
            Log.d(TAG, "onCreate: after loadAudio");
            Collections.sort(audioList, new Comparator<Audio>() {
                @Override
                public int compare(Audio audio1, Audio audio2) {
                    return audio1.getTitle().compareToIgnoreCase(audio2.getTitle());
                }
            });
        } else {
            Toast.makeText(OfflineActivityTry.this, "No Songs", Toast.LENGTH_SHORT);
        }

        int permCode = ContextCompat.checkSelfPermission(
                this, Manifest.permission.WRITE_EXTERNAL_STORAGE);

        if (permCode == PackageManager.PERMISSION_DENIED) {
            Toast.makeText(getApplicationContext(), "denied", Toast.LENGTH_SHORT).show();
            Log.d(TAG, "onCreate Permission Denied");
            Log.d(TAG, "++++++++++++++++++++++++++");
            ActivityCompat.requestPermissions(this, new String[]{
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.READ_EXTERNAL_STORAGE
            }, 300);
        } else {
            Log.d(TAG, "onCreate Permission Granted");
            Log.d(TAG, "++++++++++++++++++++++++++++");
            Toast.makeText(getApplicationContext(), "granted", Toast.LENGTH_SHORT).show();
        }

        Log.d(TAG, "onCreate: Before rv");
        rvTrackList = findViewById(R.id.rvTrackList);
        rvTrackList.setLayoutManager(new LinearLayoutManager(this));
        mediaAdapter = new MediaAdapter(this, audioList);
        rvTrackList.setAdapter(mediaAdapter);
        listView.setAdapter(adapterSwipe);
        Log.d(TAG, "onCreate: after rv");
        adapterSwipe.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void itemClick(int itemId, View view) {
                adapterSwipe.closeAllItems();
                switch (view.getId()) {
                    case R.id.Layout1:
                    Log.d(TAG, "onCreate: inside onClick");
                        Intent i = new Intent(OfflineActivityTry.this, MediaActivity.class);
                        i.putExtra("songId", itemId);
                        i.putExtra("audioList", audioList);
                        startActivity(i);
                        break;
                    case R.id.del:
                        String root = Environment.getExternalStorageDirectory().toString();
                        String path = audioList.get(itemId).getData();
                        Toast.makeText(getApplicationContext(), path, Toast.LENGTH_SHORT).show();
                        File file = new File(path);
                        Toast.makeText(getApplicationContext(), file.canWrite() + "", Toast.LENGTH_SHORT).show();
                        Boolean deleted = file.delete();
                        if (file.exists()) {
                            try {
                                file.getCanonicalFile().delete();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                        Toast.makeText(OfflineActivityTry.this, "del" + deleted, Toast.LENGTH_SHORT).show();
                        break;
                    case R.id.fav:
                        Toast.makeText(OfflineActivityTry.this, "fav", Toast.LENGTH_SHORT).show();
                        PlayListHelper.addToPlaylist(OfflineActivityTry.this.getContentResolver(),
                                audioList.get(itemId).getId(),
                                PlayListHelper.getPlaylist(getApplicationContext().getContentResolver(), "Favourites"));
                        break;
                    case R.id.fbUpload:
                        dialog.setMessage("Uploading to firebase");
                        dialog.show();
                        Toast.makeText(getApplicationContext(), "Share", Toast.LENGTH_SHORT).show();
                        uploadFile(itemId);
                        break;
                }
            }
        });

        tvTotalSongs = findViewById(R.id.tvTotalSongs);
        tvTotalSongs.setText("Total Songs : " + audioList.size());
    }

    private void uploadFile(int itemId) {
        final StorageReference filepath = storageReference.child(StorageUtil.uid).child(audioList.get(itemId).getTitle() + ".mp3");
        Uri uri = Uri.fromFile(new File(audioList.get(itemId).getData()));
        filepath.putFile(uri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                dialog.dismiss();
                filepath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        String share = uri.toString();
                        Intent si = new Intent();
                        si.setAction(Intent.ACTION_SEND);
                        si.putExtra(Intent.EXTRA_TEXT, share);
                        si.setType("text/plain");
                        startActivity(si);
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(OfflineActivityTry.this, "Failed to get downloaded url", Toast.LENGTH_SHORT).show();
                    }
                });

                Toast.makeText(getApplicationContext(), "Success", Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                dialog.dismiss();
                Toast.makeText(getApplicationContext(), "Failed" + e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; it adds items to the action bar if it's present.
        getMenuInflater().inflate(R.menu.offline_activity_try, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item here
        // The action bar will handle clicks automatically on the Home/Up button,
        // so long as you specify a parent activity in AndroidManifest - file.
        int id = item.getItemId();

        if (id == R.id.sortByDuration) {
            Collections.sort(audioList, new Comparator<Audio>() {
                @Override
                public int compare(Audio audio, Audio t1) {
                    Long a1 = audio.getDuration();
                    Long a2 = t1.getDuration();
                    return a1.compareTo(a2);
                }
            });
            item.setChecked(true);
            adapterSwipe.updateMedia(audioList);
            return true;
        } else if (id == R.id.sortByName) {
            Collections.sort(audioList, new Comparator<Audio>() {
                @Override
                public int compare(Audio audio1, Audio audio2) {
                    return audio1.getTitle().compareToIgnoreCase(audio2.getTitle());
                }
            });
            item.setChecked(true);
            adapterSwipe.updateMedia(audioList);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void loadAudio(Context context) {
        Log.d(TAG, "onCreate: inside loadAudio");
        ContentResolver resolver = getContentResolver();

        Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        String selection = MediaStore.Audio.Media.IS_MUSIC + "!=0";
        String sortOrder = MediaStore.Audio.Media.TITLE + " ASC";
        Cursor cursor = resolver.query(uri, null, selection, null, sortOrder);

        if (cursor != null && cursor.getCount() > 0) {
            audioList = new ArrayList<>();
            while (cursor.moveToNext()) {
                String data = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATA));
                Long id = cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media._ID));
                String title = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.TITLE));
                String album = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM));
                String artist = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST));
                long duration = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION));
                //Save to audioList
                audioList.add(new Audio(id, data, title, album, artist, duration));
            }
        }
        cursor.close();
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        //Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.Playlists) {
            Intent playlist = new Intent(getApplicationContext(), PlayLists.class);
            startActivity(playlist);
        } else if (id == R.id.Settings) {
            Intent i = new Intent(getApplicationContext(), SetSilentPhone.class);
            startActivity(i);
        } else if (id == R.id.Youtube) {

        } else if (id == R.id.Pedometer) {
            Intent i = new Intent(OfflineActivityTry.this, Pedometer.class);
            i.putExtra("uid", StorageUtil.uid);
            startActivity(i);
        } else if (id == R.id.Online) {

        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}