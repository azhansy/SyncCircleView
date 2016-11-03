package com.github.azhansy.synccircleview;

import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private SyncCircleView view;
    private Button btBegin;
    private Button btReset;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        view = (SyncCircleView) findViewById(R.id.sync_view);
        btBegin = (Button) findViewById(R.id.bt_begin);
        btReset = (Button) findViewById(R.id.bt_reset);
        btBegin.setOnClickListener(this);
        btReset.setOnClickListener(this);
    }

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            int progress = (int) msg.obj;
            view.setPercent(progress);

        }
    };

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.bt_begin:
                new Thread(new Runnable() {
                    @Override
                    public void run() {

                        for (int i = 0; i <= 100; i++) {
                            try {
                                Thread.sleep(50);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                            Message message = Message.obtain();
                            message.obj = i;
                            handler.sendMessage(message);
                        }
                    }
                }).start();
                break;
            case R.id.bt_reset:
                Message message = Message.obtain();
                message.obj = 0;
                handler.sendMessage(message);
                break;
        }
    }
}
