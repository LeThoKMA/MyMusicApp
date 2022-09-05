package com.example.musicapp;

import android.content.Context;
import android.content.Intent;
import android.media.MediaMetadataRetriever;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;


public class MyAdapter extends RecyclerView.Adapter<MyAdapter.MyViewHolder> {
    Context context;
    ArrayList<MusicFiles> list;

    public MyAdapter(Context context, ArrayList<MusicFiles> list) {
        this.context = context;
        this.list = list;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        holder.tv_name.setText(list.get(position).getTitle());
        holder.tv_artist.setText(list.get(position).getArtist());

        byte[] image = getAlbumArt(list.get(position).getPath());
        {
            if (image != null) {
                Glide.with(this.context).asBitmap().load(image).into(holder.imageSong);
            } else {
                Glide.with(context).load(R.drawable.ic_launcher_foreground).into(holder.imageSong);
            }
        }
        int m = position;
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, MainActivity.class);
                intent.putExtra("position", m);
               // intent.putExtra("image", image);
                context.startActivity(intent);

            }
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }


    public static class MyViewHolder extends RecyclerView.ViewHolder {
        ImageView imageSong;
        TextView tv_name, tv_artist;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            imageSong = itemView.findViewById(R.id.imageSong);
            tv_name = itemView.findViewById(R.id.nameSong);
            tv_artist = itemView.findViewById(R.id.artist);
        }

    }

    private byte[] getAlbumArt(String uri) {
        try {
            MediaMetadataRetriever retriever = new MediaMetadataRetriever();
            retriever.setDataSource(uri);
            byte[] art = retriever.getEmbeddedPicture();
            retriever.release();
            return art;
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return null;

    }
}
