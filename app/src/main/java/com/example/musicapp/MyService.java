package com.example.musicapp;

import static com.example.musicapp.NotificationWithVersionO.s;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.media.session.MediaSessionCompat;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.core.app.NotificationCompat;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;


public class MyService extends Service {
    ArrayList<MusicFiles> musicFiles3 = new ArrayList<>();
    MediaPlayer mediaPlayer = new MediaPlayer();
    boolean isPlay;
    String PRE = "presong";
    String PAUSE = "pause";
    String RUN = "run";
    String NEXT = "next";
    int position, tmp;
    Handler handler = new Handler();
    Notification notification;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {

        super.onCreate();

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        if (intent != null && intent.hasExtra("list"))
            musicFiles3 = intent.getParcelableArrayListExtra("list");

        if (intent != null && intent.hasExtra("position")) {
            tmp = intent.getIntExtra("position", -1);
            {
                if (tmp != -1 && tmp != position) {
                    position = tmp;

                    if (mediaPlayer.isPlaying()) {
                        mediaPlayer.pause();
                        mediaPlayer.stop();
                        mediaPlayer.release();
                        MusicFiles musicFiles = musicFiles3.get(position);
                        Uri uri = Uri.parse(musicFiles.getPath());
                        mediaPlayer = MediaPlayer.create(this, uri);
                        mediaPlayer.start();


                    } else {
                        MusicFiles musicFiles = musicFiles3.get(position);
                        Uri uri = Uri.parse(musicFiles.getPath());
                        mediaPlayer = MediaPlayer.create(this, uri);
                        mediaPlayer.start();


                    }
                }
            }
            isPlay = true;
            sendNotification();
            sendData(isPlay);

        }

        listenSeekBar(intent);
        threadSendCurrentTime();
        playSong(intent);
        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                Intent intent1 = new Intent(MyService.this, MyReceiver.class);
                intent1.putExtra("next", 3);
                sendBroadcast(intent1);

            }
        });

        return START_NOT_STICKY;
    }

    private void sendNotification() {

        PendingIntent contentIntentPre, contentIntentPau, contentIntent, pendingIntentActivity;
        Intent notificationIntentPrebutton = new Intent(this, MyReceiver.class);
        notificationIntentPrebutton.putExtra("pre", 0);
        //   contentIntentPre = PendingIntent.getBroadcast(this.getApplicationContext(), 0, notificationIntentPrebutton, PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);
        Intent notificationIntentPause = new Intent(this, MyReceiver.class);

        if (mediaPlayer.isPlaying()) {
            notificationIntentPause.putExtra("pause", 1);
        } else notificationIntentPause.putExtra("run", 2);
        contentIntentPau = PendingIntent.getBroadcast(this.getApplicationContext(), 1, notificationIntentPause, PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);

        Intent notificationIntentNext = new Intent(this, MyReceiver.class);
        notificationIntentNext.putExtra("next", 3);
        // contentIntent = PendingIntent.getBroadcast(this.getApplicationContext(), 2, notificationIntentNext, PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);
        Intent notifyIntent = new Intent(this, MainActivity.class);
// Set the Activity to start in a new, empty task
        notifyIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        Bundle bundle = new Bundle();
        notifyIntent.setAction("data_when_back");
        bundle.putInt("position_when_back", position);
        bundle.putBoolean("flag", isPlay);
        bundle.putInt("current_position_when_back", mediaPlayer.getCurrentPosition() / 1000);
        notifyIntent.putExtras(bundle);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            contentIntentPre = PendingIntent.getBroadcast(this.getApplicationContext(), 0, notificationIntentPrebutton, PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);

            contentIntentPau = PendingIntent.getBroadcast(this.getApplicationContext(), 1, notificationIntentPause, PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);


            contentIntent = PendingIntent.getBroadcast(this.getApplicationContext(), 2, notificationIntentNext, PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);
// Create the PendingIntent
            pendingIntentActivity = PendingIntent.getActivity(
                    this, 3, notifyIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        } else {

            contentIntentPre = PendingIntent.getBroadcast(this.getApplicationContext(), 0, notificationIntentPrebutton, PendingIntent.FLAG_UPDATE_CURRENT);


            contentIntentPau = PendingIntent.getBroadcast(this.getApplicationContext(), 1, notificationIntentPause, PendingIntent.FLAG_UPDATE_CURRENT);


            contentIntent = PendingIntent.getBroadcast(this.getApplicationContext(), 2, notificationIntentNext, PendingIntent.FLAG_UPDATE_CURRENT);

// Create the PendingIntent
            pendingIntentActivity = PendingIntent.getActivity(
                    this, 3, notifyIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT);
        }

        MediaSessionCompat mediaSession = new MediaSessionCompat(this, "tag");
        mediaSession.setActive(true);


        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, s)
                .setSmallIcon(R.drawable.ic_baseline_music_note_24)
                .setContentTitle(musicFiles3.get(position).getTitle())
                .setContentText(musicFiles3.get(position).getArtist())
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                //  .setLargeIcon(bitmap)
                .setContentIntent(pendingIntentActivity)
                .setStyle(new androidx.media.app.NotificationCompat.MediaStyle()
                        .setShowActionsInCompactView(0, 1, 2)
                        .setMediaSession(mediaSession.getSessionToken()));
        if (isPlay) {
            builder.addAction(R.drawable.ic_baseline_skip_previous_24, "previous", contentIntentPre)
                    .addAction(R.drawable.ic_baseline_pause_circle_filled_24, "pause", contentIntentPau)
                    .addAction(R.drawable.ic_baseline_skip_next_24, "next", contentIntent);
        } else {
            builder.addAction(R.drawable.ic_baseline_skip_previous_24, "previous", contentIntentPre)
                    .addAction(R.drawable.ic_baseline_play_arrow_24, "run", contentIntentPau)
                    .addAction(R.drawable.ic_baseline_skip_next_24, "next", contentIntent);
        }
        if (getAlbumArt(musicFiles3.get(position).getPath()) != null) {
            Bitmap bitmap;
            ByteArrayInputStream is = new ByteArrayInputStream(getAlbumArt(musicFiles3.get(position).getPath()));
            bitmap = BitmapFactory.decodeStream(is);
            builder.setLargeIcon(bitmap);
        } else {
            Drawable drawable = AppCompatResources.getDrawable(getApplicationContext(), R.drawable.ic_launcher_foreground);
            builder.setLargeIcon(drawableToBitmap(drawable));

        }
        notification = builder.build();
        startForeground(1, notification);
    }

    private void clickPreSong() {
        if (mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
            mediaPlayer.stop();
            mediaPlayer.release();
            if (position > 0) {
                position = (position - 1) % musicFiles3.size();
            }
            if (position == 0) {
                position = musicFiles3.size() - 1;
            }
            Uri urii = Uri.parse(musicFiles3.get(position).getPath());
            mediaPlayer = MediaPlayer.create(this, urii);
            mediaPlayer.start();
        } else {
            mediaPlayer.release();
            if (position > 0) {
                position = (position - 1) % musicFiles3.size();
            }
            if (position == 0) {
                position = musicFiles3.size() - 1;
            }
            Uri urii = Uri.parse(musicFiles3.get(position).getPath());
            mediaPlayer = MediaPlayer.create(this, urii);
            mediaPlayer.start();

        }
        sendNotification();
        sendData(isPlay);


    }

    private void clickPause() {
        if (mediaPlayer.isPlaying()) {
        } else {
            mediaPlayer.start();
            isPlay = true;
        }
    }

    private void clickNext() {
        if (mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
            mediaPlayer.stop();
            mediaPlayer.release();
            position = (position + 1) % musicFiles3.size();
            Uri urii = Uri.parse(musicFiles3.get(position).getPath());
            mediaPlayer = MediaPlayer.create(this, urii);
            mediaPlayer.start();

        } else {
            mediaPlayer.release();
            position = (position + 1) % musicFiles3.size();
            Uri urii = Uri.parse(musicFiles3.get(position).getPath());
            mediaPlayer = MediaPlayer.create(this, urii);
            mediaPlayer.start();

        }
        sendNotification();
        sendData(isPlay);
    }


    private void playSong(@NonNull Intent intent) {
        if (intent.getStringExtra("pre") != null) {
            if (intent.getStringExtra("pre").equals(PRE)) {
                clickPreSong();
            }
        } else if (intent.getStringExtra("pause") != null) {
            if (intent.getStringExtra("pause").equals(PAUSE)) {
                mediaPlayer.pause();
                isPlay = false;
                sendNotification();
                sendData(isPlay);


            }
        } else if (intent.getStringExtra("run") != null) {
            if (intent.getStringExtra("run").equals(RUN)) {
                mediaPlayer.start();
                isPlay = true;
                sendNotification();
                sendData(isPlay);
            }
        } else if (intent.getStringExtra("next") != null) {
            if (intent.getStringExtra("next").equals(NEXT)) {
                clickNext();

            }
        }


    }

    private byte[] getAlbumArt(String uri) {
        try {
            MediaMetadataRetriever retriever = new MediaMetadataRetriever();
            retriever.setDataSource(uri);
            byte[] art = retriever.getEmbeddedPicture();
            retriever.release();
            return art;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private void sendData(boolean check) {
        Intent intent1 = new Intent("send_data_to_activity");
        Bundle bundle = new Bundle();
        bundle.putInt("position_service", position);
        bundle.putBoolean("status", check);
        intent1.putExtras(bundle);
        sendBroadcast(intent1);
    }

    public static Bitmap drawableToBitmap(Drawable drawable) {
        if (drawable instanceof BitmapDrawable) {
            return ((BitmapDrawable) drawable).getBitmap();
        }

        int width = drawable.getIntrinsicWidth();
        width = width > 0 ? width : 1;
        int height = drawable.getIntrinsicHeight();
        height = height > 0 ? height : 1;

        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);

        return bitmap;
    }

    private void threadSendCurrentTime() {

        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (mediaPlayer != null && isPlay) {
                    Intent intent = new Intent("send_current_time");
                    intent.putExtra("curent_position", mediaPlayer.getCurrentPosition() / 1000);
                    sendBroadcast(intent);
                } else {
                    handler.removeCallbacks(this);
                }
                handler.postDelayed(this, 1000);
            }
        }, 1000);
    }

    private void listenSeekBar(Intent intent) {
        if (intent.hasExtra("progress")) {
            if (intent.getIntExtra("progress", -1) > -1) {
                mediaPlayer.seekTo(intent.getIntExtra("progress", -1) * 1000);
            }
        }
    }


}


