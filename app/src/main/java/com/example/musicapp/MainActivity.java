package com.example.musicapp;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.media.MediaMetadataRetriever;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    ImageButton btnback, btn_next, btn_pause, btn_backSong;
    ImageView imageSong;
    TextView tv_nameSong, tv_duration_played, tv_duration_total;
    ArrayList<MusicFiles> musicFiles2;
    SeekBar seekBar;
    static int position, positionSV, curentPosition;
    static boolean flag;
    Handler handler;
    Receiver receiver = new Receiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            super.onReceive(context, intent);
            Bundle bundle = intent.getExtras();
            if (bundle == null) {
                return;
            } else {

                position = bundle.getInt("position_service");
                seekBar.setMax(Integer.parseInt(musicFiles2.get(position).getDuration()) / 1000);
                if (positionSV != position) {
                    positionSV = position;


                    byte[] art = getAlbumArt(musicFiles2.get(position).getPath());
                    if (art != null) {
                        Glide.with(MainActivity.this).asBitmap().load(art).into(imageSong);
                    } else {
                        Glide.with(MainActivity.this).load(R.drawable.ic_launcher_foreground).into(imageSong);
                    }
                    int total_time = Integer.parseInt(musicFiles2.get(position).getDuration()) / 1000;
                    tv_duration_total.setText(fommatedTime(total_time));
                    tv_nameSong.setText(musicFiles2.get(position).getTitle());
                }


                flag = bundle.getBoolean("status");
                if (flag) {
                    btn_pause.setImageResource(R.drawable.ic_baseline_pause_circle_filled_24);
                } else
                    btn_pause.setImageResource(R.drawable.ic_baseline_play_arrow_24);

            }
        }
    };
    CurrentTimeReceiver broadcastReceiver = new CurrentTimeReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            curentPosition = intent.getIntExtra("curent_position", 0);
            seekBar.setProgress(curentPosition);
            tv_duration_played.setText(fommatedTime(curentPosition));

        }

    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        handler = new Handler();
        setPermission();
        addControl();
        playSong();
        IntentFilter intentFilter = new IntentFilter("send_current_time");
        registerReceiver(broadcastReceiver, intentFilter);
        IntentFilter intent = new IntentFilter("send_data_to_activity");
        registerReceiver(receiver, intent);

        addEvent();
        if(getIntent().getAction()!=null) {
            if (getIntent().getAction().equals("data_when_back")) {
                Bundle bundle =getIntent().getExtras();

                position = bundle.getInt("position_when_back", -1);
                seekBar.setMax(Integer.parseInt(musicFiles2.get(position).getDuration()) / 1000);
                flag= bundle.getBoolean("flag");
                seekBar.setProgress(bundle.getInt("current_position_when_back", -1));
                tv_duration_played.setText(fommatedTime(bundle.getInt("current_position_when_back", -1)));
                byte[] art = getAlbumArt(musicFiles2.get(position).getPath());
                if (art != null) {
                    Glide.with(MainActivity.this).asBitmap().load(art).into(imageSong);
                } else {
                    Glide.with(MainActivity.this).load(R.drawable.ic_launcher_foreground).into(imageSong);
                }
                int total_time = Integer.parseInt(musicFiles2.get(position).getDuration()) / 1000;
                tv_duration_total.setText(fommatedTime(total_time));
                tv_nameSong.setText(musicFiles2.get(position).getTitle());
                if (flag) {
                    btn_pause.setImageResource(R.drawable.ic_baseline_pause_circle_filled_24);
                } else
                    btn_pause.setImageResource(R.drawable.ic_baseline_play_arrow_24);
            }
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(receiver);
        unregisterReceiver(broadcastReceiver);

    }

    private void addEvent() {
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                Intent intent = new Intent(MainActivity.this, MyReceiver.class);
                intent.putExtra("pause", 1);
                sendBroadcast(intent);
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

                Intent intent = new Intent(MainActivity.this, MyReceiver.class);
                intent.putExtra("progress", seekBar.getProgress());
                intent.putExtra("run", 2);
                sendBroadcast(intent);

            }

        });

        btnback.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, ListActivity.class);
                startActivity(intent);
            }
        });

        btn_backSong.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playPrebtnClick();
            }
        });
        btn_pause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playPausebtnClick();
            }
        });
        btn_next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playNextbtnClick();
            }
        });
    }

    private String fommatedTime(int currentTime) {
        String totalout = "";
        String totalnew = "";
        String seconds = String.valueOf(currentTime % 60);
        String minute = String.valueOf(currentTime / 60);
        totalout = minute + ":" + seconds;
        totalnew = minute + ":" + "0" + seconds;
        if (seconds.length() == 1) {
            return totalnew;
        } else {
            return totalout;
        }
    }

    private void setPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.READ_EXTERNAL_STORAGE)) {
            } else {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                        1);
            }
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            } else {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        1);
            }
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.FOREGROUND_SERVICE) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.FOREGROUND_SERVICE)) {
            } else {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.FOREGROUND_SERVICE},
                        1);
            }
        }
    }

    private void addControl() {
        btnback = findViewById(R.id.btn_back);
        btn_next = findViewById(R.id.btn_next);
        btn_pause = findViewById(R.id.img_btn_pause);
        btn_backSong = findViewById(R.id.img_btn_back_song);
        tv_nameSong = findViewById(R.id.tv_namesong);
        seekBar = findViewById(R.id.seekBar2);
        musicFiles2 = ListActivity.musicFiles;
        tv_duration_played = findViewById(R.id.tv_duration_played);
        tv_duration_total = findViewById(R.id.tv_duration_total);
        imageSong = findViewById(R.id.img_of_song);

    }

    private void playSong() {

        Intent intent = getIntent();
       position = intent.getIntExtra("position", -1);
        if(position!=-1) {
            Intent intentsv = new Intent(this, MyService.class);
            intentsv.putParcelableArrayListExtra("list", musicFiles2);
            intentsv.putExtra("position", position);
            ContextCompat.startForegroundService(this, intentsv);
            byte[] image = getAlbumArt(musicFiles2.get(position).getPath());
            if (image != null) {
                Glide.with(this).asBitmap().load(image).into(imageSong);
            } else {
                Glide.with(this).load(R.drawable.ic_launcher_foreground).into(imageSong);
            }

            int total_time = Integer.parseInt(musicFiles2.get(position).getDuration()) / 1000;
            tv_duration_total.setText(fommatedTime(total_time));
            tv_nameSong.setText(musicFiles2.get(position).getTitle());
        }

        if (musicFiles2 != null) {
            btn_pause.setImageResource(R.drawable.ic_baseline_pause_circle_filled_24);
        }
        flag = true;

    }

    private void playPrebtnClick() {

        Intent intent = new Intent(this, MyReceiver.class);
        intent.putExtra("pre", 0);
        sendBroadcast(intent);

    }


    private void playPausebtnClick() {
        if (flag) {
            btn_pause.setImageResource(R.drawable.ic_baseline_play_arrow_24);
            flag = false;
            Intent intent = new Intent(this, MyReceiver.class);
            intent.putExtra("pause", 1);
            sendBroadcast(intent);

        } else {
            btn_pause.setImageResource(R.drawable.ic_baseline_pause_circle_filled_24);
            flag = true;
            Intent intent = new Intent(this, MyReceiver.class);
            intent.putExtra("run", 2);
            sendBroadcast(intent);

        }
    }


    private void playNextbtnClick() {

        Intent intent = new Intent(this, MyReceiver.class);
        intent.putExtra("next", 3);
        sendBroadcast(intent);

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
