package music.player;

import android.os.IBinder;

public class LocalBinder extends IBinder {
    public MediaPlayerService getService() {
        return MediaPlayerService.this;
    }
}
