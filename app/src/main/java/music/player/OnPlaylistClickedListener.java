package music.player;

import android.content.Context;

public interface OnPlaylistClickedListener {
    void onPlaylistClicked(int itemid, Context context, Long pid);
}
