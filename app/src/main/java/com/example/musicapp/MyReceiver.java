package com.example.musicapp;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class MyReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Intent intentService = new Intent(context, MyService.class);
        if (intent.getIntExtra("pre", -1) == 0) {
            intentService.putExtra("pre", "presong");
            context.startService(intentService);

        } else if (intent.getIntExtra("pause", -1) == 1) {
            intentService.putExtra("pause", "pause");
            context.startService(intentService);
        } else if (intent.getIntExtra("run", -1) == 2) {

            intentService.putExtra("run", "run");
            context.startService(intentService);
        } else if (intent.getIntExtra("next", -1) == 3) {
            intentService.putExtra("next", "next");
            context.startService(intentService);
        }
if(intent.getIntExtra("progress", -1)>-1)
{
    int progress=intent.getIntExtra("progress", -1);
    intentService.putExtra("progress",progress);
    context.startService(intentService);
}
    }
}
