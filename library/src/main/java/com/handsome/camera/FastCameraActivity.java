package com.handsome.camera;

import android.Manifest;
import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.util.List;

import top.zibin.luban.Luban;
import top.zibin.luban.OnCompressListener;

public class FastCameraActivity extends AppCompatActivity {

    private static String PICTURES_DIR;
    private static String CROP_DIR;
    private static String COMPRESS_DIR;

    private static final String TAG = FastCameraActivity.class.getSimpleName();

    private static final int REQUEST_CAMERA = 0x158;
    private static final int IMAGE_CROP_CODE = 0x159;
    private static final int CAMERA_REQUEST_CODE = 0x160;

    private File cropImageFile;
    private File tempPhotoFile;

    private FastCamera config;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fast_camera);

        PICTURES_DIR = getExternalFilesDir(Environment.DIRECTORY_PICTURES).getAbsolutePath();
        CROP_DIR = FastCameraUtils.createDir(PICTURES_DIR + File.separatorChar + "Crop");
        COMPRESS_DIR = FastCameraUtils.createDir(PICTURES_DIR + File.separatorChar + "Compress");

        config = (FastCamera) getIntent().getSerializableExtra("config");
        if (config == null)
            return;

        camera();
    }

    /**
     * 开启拍照
     */
    private void camera() {
        if (Build.VERSION.SDK_INT >= 24) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                    != PackageManager.PERMISSION_GRANTED
                    || ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE}, CAMERA_REQUEST_CODE);
                return;
            }
        }

        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (cameraIntent.resolveActivity(getPackageManager()) != null) {
            tempPhotoFile = FastCameraUtils.createTempImageFile(this);
            Log.i(TAG, tempPhotoFile.getAbsolutePath());

            Uri uri = FileProvider.getUriForFile(this, FastCameraUtils.getApplicationId(this) + ".provider", tempPhotoFile);
            List<ResolveInfo> resInfoList = getPackageManager().queryIntentActivities(cameraIntent, PackageManager.MATCH_DEFAULT_ONLY);
            for (ResolveInfo resolveInfo : resInfoList) {
                String packageName = resolveInfo.activityInfo.packageName;
                grantUriPermission(packageName, uri, Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);
            }
            cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
            startActivityForResult(cameraIntent, REQUEST_CAMERA);
        } else {
            Toast.makeText(this, "open camera failure", Toast.LENGTH_SHORT).show();
        }
    }

    private void crop(String imagePath) {
        cropImageFile = new File(CROP_DIR + "/" + System.currentTimeMillis() + ".jpg");

        Intent intent = new Intent("com.android.camera.action.CROP");
        intent.setDataAndType(getImageContentUri(new File(imagePath)), "image/*");
        intent.putExtra("crop", "true");
        intent.putExtra("aspectX", config.aspectX);
        intent.putExtra("aspectY", config.aspectY);
        intent.putExtra("outputX", config.outputX);
        intent.putExtra("outputY", config.outputY);
        intent.putExtra("scale", true);
        intent.putExtra("scaleUpIfNeeded", true);
        intent.putExtra("return-data", false);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(cropImageFile));
        intent.putExtra("outputFormat", Bitmap.CompressFormat.JPEG.toString());
        intent.putExtra("noFaceDetection", true);

        startActivityForResult(intent, IMAGE_CROP_CODE);
    }

    public Uri getImageContentUri(File imageFile) {
        String filePath = imageFile.getAbsolutePath();
        Cursor cursor = getContentResolver().query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                new String[]{MediaStore.Images.Media._ID},
                MediaStore.Images.Media.DATA + "=? ",
                new String[]{filePath}, null);

        if (cursor != null && cursor.moveToFirst()) {
            int id = cursor.getInt(cursor.getColumnIndex(MediaStore.MediaColumns._ID));
            Uri baseUri = Uri.parse("content://media/external/images/media");
            cursor.close();
            return Uri.withAppendedPath(baseUri, "" + id);
        } else {
            if (imageFile.exists()) {
                ContentValues values = new ContentValues();
                values.put(MediaStore.Images.Media.DATA, filePath);
                if (cursor != null) {
                    cursor.close();
                }
                return getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
            } else {
                return null;
            }
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == IMAGE_CROP_CODE && resultCode == RESULT_OK) {
            complete(cropImageFile.getAbsolutePath());

        } else if (requestCode == REQUEST_CAMERA) {
            if (resultCode == Activity.RESULT_OK) {
                if (tempPhotoFile != null) {
                    if (config.needCrop) {
                        crop(tempPhotoFile.getAbsolutePath());

                    } else if (config.needCompress) {
                        Luban.with(this)
                                .load(tempPhotoFile)
                                .setTargetDir(COMPRESS_DIR)
                                .setCompressListener(onCompressListener)
                                .launch();

                    } else {
                        complete(tempPhotoFile.getAbsolutePath());
                    }
                }
            } else {
                if (tempPhotoFile != null && tempPhotoFile.exists()) {
                    tempPhotoFile.delete();
                }
                finish();
            }
        } else {
            finish();
        }
    }

    private OnCompressListener onCompressListener = new OnCompressListener() {
        @Override
        public void onStart() {

        }

        @Override
        public void onSuccess(File file) {
            complete(file.getAbsolutePath());
        }

        @Override
        public void onError(Throwable e) {
            Toast.makeText(FastCameraActivity.this, "image compress error", Toast.LENGTH_SHORT).show();
        }
    };

    /**
     * @param path
     */
    private void complete(String path) {
        Intent intent = new Intent();
        intent.putExtra("result", path);
        setResult(RESULT_OK, intent);
        finish();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case CAMERA_REQUEST_CODE:
                if (grantResults.length >= 2 && grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                    camera();
                } else {
                    Toast.makeText(this, "permission camera denied", Toast.LENGTH_SHORT).show();
                }
                break;
            default:
                break;
        }
    }

}
