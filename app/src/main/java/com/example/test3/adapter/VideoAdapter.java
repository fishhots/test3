package com.example.test3.adapter;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.example.test3.R;
import com.example.test3.model.VideoItem;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.ui.StyledPlayerView;
import java.util.List;

public class VideoAdapter extends RecyclerView.Adapter<VideoAdapter.VideoViewHolder> {
    private Context context;
    private List<VideoItem> videoItems;
    private ExoPlayer currentPlayer;

    public VideoAdapter(Context context, List<VideoItem> videoItems) {
        this.context = context;
        this.videoItems = videoItems;
    }

    @NonNull
    @Override
    public VideoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_video, parent, false);
        return new VideoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull VideoViewHolder holder, int position) {
        VideoItem item = videoItems.get(position);

        // 设置视频标题和作者
        holder.titleText.setText(item.getTitle());
        holder.authorText.setText(item.getAuthor());

        // 设置互动数据
        holder.likeCount.setText(String.valueOf(item.getLikeCount()));
        holder.commentCount.setText(String.valueOf(item.getCommentCount()));
        holder.shareCount.setText(String.valueOf(item.getShareCount()));

        // 使用Glide加载封面图
        Glide.with(context)
                .load(item.getCoverUrl())
                .into(holder.coverImage);

        // 初始化ExoPlayer
        if (holder.player == null) {
            holder.player = new ExoPlayer.Builder(context).build();
            holder.playerView.setPlayer(holder.player);
        }

        // 准备视频
        MediaItem mediaItem = MediaItem.fromUri(Uri.parse(item.getVideoUrl()));
        holder.player.setMediaItem(mediaItem);
        holder.player.prepare();

        // 设置点击事件
        holder.coverImage.setOnClickListener(v -> {
            holder.coverImage.setVisibility(View.GONE);
            holder.playerView.setVisibility(View.VISIBLE);

            // 停止当前正在播放的视频
            if (currentPlayer != null && currentPlayer != holder.player) {
                currentPlayer.stop();
            }

            holder.player.play();
            currentPlayer = holder.player;
        });
    }

    @Override
    public int getItemCount() {
        return videoItems.size();
    }

    public void releaseAllPlayers() {
        if (currentPlayer != null) {
            currentPlayer.release();
            currentPlayer = null;
        }
    }

    static class VideoViewHolder extends RecyclerView.ViewHolder {
        StyledPlayerView playerView;
        ImageView coverImage;
        TextView titleText;
        TextView authorText;
        TextView likeCount;
        TextView commentCount;
        TextView shareCount;
        ExoPlayer player;

        VideoViewHolder(View itemView) {
            super(itemView);
            playerView = itemView.findViewById(R.id.player_view);
            coverImage = itemView.findViewById(R.id.cover_image);
            titleText = itemView.findViewById(R.id.title_text);
            authorText = itemView.findViewById(R.id.author_text);
            likeCount = itemView.findViewById(R.id.like_count);
            commentCount = itemView.findViewById(R.id.comment_count);
            shareCount = itemView.findViewById(R.id.share_count);
        }
    }
}