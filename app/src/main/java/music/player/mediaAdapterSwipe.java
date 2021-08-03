package music.player;


import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.daimajia.swipe.adapters.BaseSwipeAdapter;

import java.util.ArrayList;

public class mediaAdapterSwipe extends BaseSwipeAdapter {
    Context context;
    ArrayList<Audio> audioList;
    OnItemClickListener onItemClickListener;

    public mediaAdapterSwipe(Context context, ArrayList<Audio> audioList) {
        this.context = context;
        this.audioList = audioList;
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    public void updateMedia(ArrayList<Audio> newAudioArrayList) {
        this.audioList = newAudioArrayList;
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return audioList.size();
    }

    @Override
    public Audio getItem(int i) {
        return audioList.get(i);
    }

    @Override
    public int getSwipeLayoutResourceId(int position) {
        return R.id.mswipe;
    }


    @Override
    public View generateView(int position, ViewGroup parent) {
        if (getItemViewType(position) == 0) {
            return LayoutInflater.from(context).inflate(R.layout.swipe_list_item_track, parent, false);
        } else {
            return LayoutInflater.from(context).inflate(R.layout.swipe_list_item_white, parent, false);
        }
    }

    @Override
    public int getViewTypeCount() {
        return 2;
    }

    @Override
    public int getItemViewType(int position) {
        if (position % 2 == 0) {
            return 0;
        }
        return 1;
    }

    @Override
    public void fillValues(final int p, View convertView) {
        TextView tv = convertView.findViewById(R.id.tvTrackName);
        tv.setText(getItem(p).getTitle());
        convertView.findViewById(R.id.fav).setOnClickListener(v -> onItemClickListener.itemClick(p, v));
        convertView.findViewById(R.id.del).setOnClickListener(v -> onItemClickListener.itemClick(p, v));
        convertView.findViewById(R.id.Layout1).setOnClickListener(v -> onItemClickListener.itemClick(p, v));
        convertView.findViewById(R.id.fbupload).setOnClickListener(v -> onItemClickListener.itemClick(p, v));
    }

    @Override
    public long getItemId(int i) {
        return i;
    }
}
