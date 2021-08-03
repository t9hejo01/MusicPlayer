package music.player;

import android.content.Context;

public interface OnPlaylistClickedListener {
    void onPlaylistClicked(int itemId, Context context, Long pid);
}
