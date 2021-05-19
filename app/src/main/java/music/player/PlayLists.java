package music.player;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;

public class PlayLists extends AppCompatActivity {
    ArrayList<Playlist> playlistArrayList;
    ListView listView;
    PlaylistAdapter playlistAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play_lists);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Long inserted = PlayListHelper.createPlaylist(getApplicationContext().getContentResolver(), "mFavourites");
                if (inserted != -1) {
                    playlistArrayList.add(new Playlist(inserted, "mFavourites"));
                    playlistAdapter.notifyDataSetChanged();
                } else {
                    Toast.makeText(getApplicationContext(), "Playlist already\n exist", Toast.LENGTH_SHORT).show();
                }
            }
        });
        playlistArrayList = PlayListHelper.getAllPlayLists(PlayLists.this);
        listView = findViewById(R.id.lvPlaylists);
        playlistAdapter = new PlaylistAdapter(PlayLists.this, playlistArrayList, new OnPlaylistClickedListener() {
            @Override
            public void onPlaylistClicked(int itemid, Context context, Long pid) {
                if (itemid != -1) {
                    PlayListHelper.deletePlaylist(context.getContentResolver(), pid);
                    playlistArrayList.remove(itemid);
                    playlistAdapter.closeAllItems();
                    playlistAdapter.notifyDataSetChanged();
                } else {
                    try {
                        Intent offlineAct = new Intent(context, OfflineActivityTry.class);
                        offlineAct.putExtra("playlistID", pid);
                        startActivity(offlineAct);
                    } catch (Exception e) {
                        Toast.makeText(context, e.toString(), Toast.LENGTH_SHORT).show();
                    }
                }
            }
        }, null);
        listView.setAdapter(playlistAdapter);
    }
}