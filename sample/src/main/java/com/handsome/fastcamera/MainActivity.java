package com.handsome.fastcamera;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.handsome.camera.FastCamera;

public class MainActivity extends AppCompatActivity {

    private int REQUEST_CAMERA_CODE = 0x05;
    private TextView tv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        tv = (TextView) findViewById(R.id.tv);
    }

    public void open_camera(View view) {
        FastCamera.with(this)
                .requestCode(REQUEST_CAMERA_CODE)
                .needCompress(true)
                .start();
    }

    public void open_camera_crop(View view) {
        FastCamera.with(this)
                .requestCode(REQUEST_CAMERA_CODE)
                .needCrop(true)
                .cropSize(1, 1, 800, 800)
                .start();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CAMERA_CODE && resultCode == RESULT_OK && data != null) {
            String path = data.getStringExtra("result");
            tv.setText("The Picture in path:" + path);
        }
    }
}
