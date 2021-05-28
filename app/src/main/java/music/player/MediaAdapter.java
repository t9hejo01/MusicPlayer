package music.player;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;


import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class MediaAdapter extends RecyclerView.Adapter<MediaAdapter.MediaViewHolder> {
    Context context;
    ArrayList<Audio> audioList;
    OnItemClickListener onItemClickListener;

    public MediaAdapter(Context context, ArrayList<Audio> audioList) {
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
    public MediaViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View itemView = layoutInflater.inflate(R.layout.list_item_tracks, parent, false);
        return new MediaViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(MediaViewHolder holder, int position) {
        Audio audio = audioList.get(position);
        holder.tvTitle.setText(audio.getTitle());
        holder.thisView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onItemClickListener.itemClick(position, v);
            }
        });
    }

    @Override
    public int getItemCount() {
        return audioList.size();
    }

    class MediaViewHolder extends RecyclerView.ViewHolder {
        View thisView;
        TextView tvTitle;

        public MediaViewHolder(View itemView) {
            super(itemView);
            thisView = itemView;
            tvTitle = itemView.findViewById(R.id.tvTitle);
        }
    }
}
