package com.shinow.qrscan;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;

import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.common.MethodChannel.MethodCallHandler;
import io.flutter.plugin.common.MethodChannel.Result;
import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.PluginRegistry;
import io.flutter.plugin.common.PluginRegistry.Registrar;
import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;

import com.google.zxing.BarcodeFormat;
import com.king.zxing.CameraScan;
import com.king.zxing.util.CodeUtils;
import com.shinow.qrscan.util.UriUtils;

import java.io.ByteArrayOutputStream;


public class QrscanPlugin implements MethodCallHandler, PluginRegistry.ActivityResultListener {

    private Result result = null;
    private Activity activity;
    private int REQUEST_CODE = 100;
    private int REQUEST_IMAGE = 101;
    public static final int RC_CAMERA = 0X01;
    public static final int RC_READ_PHOTO = 0X02;

    public static void registerWith(Registrar registrar) {
        MethodChannel channel = new MethodChannel(registrar.messenger(), "qr_scan");
        QrscanPlugin plugin = new QrscanPlugin(registrar.activity());
        channel.setMethodCallHandler(plugin);
        registrar.addActivityResultListener(plugin);
    }

    public QrscanPlugin(Activity activity) {
        this.activity = activity;
    }

    @Override
    public void onMethodCall(MethodCall call, Result result) {
        switch (call.method) {
            case "scan":
                this.result = result;
                Boolean isShowSelf = (Boolean) call.arguments;
                if (isShowSelf == null) {
                    isShowSelf = false;
                }
                showBarcodeView(isShowSelf);
                break;
            case "scan_photo":
                this.result = result;
                choosePhotos();
                break;
            case "scan_path":
                this.result = result;
                String path = (String) call.arguments;
                parsePhoto(path);
                break;
            case "generate_barcode":
                this.result = result;
                createBarCode((String) call.arguments);
                break;
            default:
                result.notImplemented();
                break;
        }
    }

    private void parsePhoto(final String path) {
        if (TextUtils.isEmpty(path)) {
            return;
        }
        //异步解析
        asyncThread(new Runnable() {
            @Override
            public void run() {
                final String data = CodeUtils.parseCode(path);
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Log.d("Jenly", "result:" + data);
                        result.success(data);
                    }
                });

            }
        });

    }

    private void asyncThread(Runnable runnable) {
        new Thread(runnable).start();
    }

    private void showBarcodeView(boolean isShowSelf) {
        Intent intent = new Intent(activity, CustomActivity.class);
        intent.putExtra("isShowSelf", isShowSelf);
        activity.startActivityForResult(intent, REQUEST_CODE);
    }

    @AfterPermissionGranted(RC_READ_PHOTO)
    private void checkExternalStoragePermissions() {
        String[] perms = {Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE};
        if (EasyPermissions.hasPermissions(activity, perms)) {//有权限
            choosePhotos();
        } else {
            EasyPermissions.requestPermissions(this, "请允许获取使用相册权限",
                    RC_READ_PHOTO, perms);
        }
    }

    private void choosePhotos() {
        Intent pickIntent = new Intent(Intent.ACTION_PICK,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        pickIntent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
        activity.startActivityForResult(pickIntent, REQUEST_IMAGE);
    }

    /**
     * 生成条形码
     *
     * @param content
     */
    private void createBarCode(final String content) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                //生成条形码相关放在子线程里面
                final Bitmap bitmap = CodeUtils.createBarCode(content, BarcodeFormat.CODE_128, 800, 200, null, true);
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        ByteArrayOutputStream baos = new ByteArrayOutputStream();
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                        byte[] datas = baos.toByteArray();
                        result.success(datas);
                    }
                });
            }
        }).start();
    }

    @Override
    public boolean onActivityResult(int code, int resultCode, Intent intent) {
        if (code == REQUEST_CODE) {
            if (resultCode == Activity.RESULT_OK && intent != null) {
                Bundle bundle = intent.getExtras();
                if (bundle != null) {
                    String barcode = bundle.getString(CameraScan.SCAN_RESULT);
                    this.result.success(barcode);
                }
            } else {
                String errorCode = intent != null ? intent.getStringExtra("ERROR_CODE") : null;
                if (errorCode != null) {
                    this.result.success(null);
                }
            }
            return true;
        } else if (code == REQUEST_IMAGE) {
            if (intent != null) {
                final String path = UriUtils.getImagePath(activity, intent);
                parsePhoto(path);
            }
            return true;
        }
        return false;
    }
}