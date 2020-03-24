package com.bugslayers.qma;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final TextView timerTv = findViewById(R.id.timer_tv);
        final ProgressBar progressBar = findViewById(R.id.progress_half);
        final LinearLayout sendReportBtn = findViewById(R.id.send_report_btn);
        LinearLayout nearByBtn = findViewById(R.id.near_by_btn);
        LinearLayout statsBtn = findViewById(R.id.stats_btn);
        //sendReportBtn.setEnabled(false);

        final Dialog dialog = onCreateDialog();

        final Handler handler = new Handler();
        final Runnable r = new Runnable() {
            public void run() {//1800000
                handler.postDelayed(this, 1860000);
                new CountDownTimer(1800000, 1000) {
                    @Override
                    public void onTick(long l) {
                        dialog.dismiss();
                        timerTv.setText(new SimpleDateFormat("HH:mm:ss").format(new Date(l)));
                        progressBar.setProgress((Integer.parseInt(new SimpleDateFormat("HH").format(new Date(l)))/1800000)*100);
                    }

                    @Override
                    public void onFinish() {
                        sendReportBtn.setEnabled(true);
                        dialog.show();
                        DisplayMetrics metrics = getResources().getDisplayMetrics();
                        int width = metrics.widthPixels;
                        int height = metrics.heightPixels;
                        dialog.getWindow().setLayout(width, height);
                        dialog.getWindow().setBackgroundDrawable(getResources().getDrawable(android.R.color.transparent));
                    }
                }.start();
            }
        };

        handler.postDelayed(r, 0);

        sendReportBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivity.this, SendReportActivity.class));
            }
        });

        statsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivity.this, StatsActivity.class));
            }
        });

        nearByBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivity.this, NearByActivity.class));
            }
        });
    }

    private Dialog onCreateDialog(){
        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.report_submitted_dialog_layout);

        TextView tv = dialog.findViewById(R.id.update_tv);
        tv.setText("Please, update your health report");

        Button doneBtn = dialog.findViewById(R.id.check_btn);
        doneBtn.setText("Submit Now");
        doneBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivity.this, SendReportActivity.class));
            }
        });

        return dialog;
    }
}
