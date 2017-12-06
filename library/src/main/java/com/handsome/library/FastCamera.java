package com.handsome.library;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.Fragment;

import java.io.Serializable;

import top.zibin.luban.Luban;

/**
 * =====作者=====
 * 许英俊
 * =====时间=====
 * 2017/12/5.
 */
public class FastCamera implements Serializable {

    public int requestCode;
    public boolean needCrop;
    public boolean needCompress;

    public int aspectX = 1;
    public int aspectY = 1;
    public int outputX = 500;
    public int outputY = 500;

    public FastCamera(Builder builder) {
        this.requestCode = builder.requestCode;
        this.needCrop = builder.needCrop;
        this.needCompress = builder.needCompress;
        this.aspectX = builder.aspectX;
        this.aspectY = builder.aspectY;
        this.outputX = builder.outputX;
        this.outputY = builder.outputY;
    }

    public static FastCamera.Builder with(Activity activity) {
        return new FastCamera.Builder(activity);
    }

    public static FastCamera.Builder with(Fragment fragment) {
        return new FastCamera.Builder(fragment);
    }


    public static class Builder implements Serializable {

        private int requestCode = 0x156;
        private boolean needCrop = false;
        private boolean needCompress = false;
        private Activity activity;
        private Fragment fragment;

        private int aspectX = 1;
        private int aspectY = 1;
        private int outputX = 400;
        private int outputY = 400;

        public Builder(Activity activity) {
            this.activity = activity;
        }

        public Builder(Fragment fragment) {
            this.fragment = fragment;
        }

        public Builder needCrop(boolean needCrop) {
            this.needCrop = needCrop;
            return this;
        }

        public Builder needCompress(boolean needCompress) {
            this.needCompress = needCompress;
            return this;
        }

        public Builder requestCode(int requestCode) {
            this.requestCode = requestCode;
            return this;
        }

        public Builder cropSize(int aspectX, int aspectY, int outputX, int outputY) {
            this.aspectX = aspectX;
            this.aspectY = aspectY;
            this.outputX = outputX;
            this.outputY = outputY;
            return this;
        }

        private FastCamera build() {
            return new FastCamera(this);
        }

        public void start() {
            if (this.activity != null) {
                Intent intent = new Intent(activity, FastCameraActivity.class);
                intent.putExtra("config", build());
                activity.startActivityForResult(intent, this.requestCode);
            }
            if (fragment != null) {
                Intent intent = new Intent(fragment.getActivity(), FastCameraActivity.class);
                intent.putExtra("config", build());
                fragment.startActivityForResult(intent, this.requestCode);
            }
        }
    }


}