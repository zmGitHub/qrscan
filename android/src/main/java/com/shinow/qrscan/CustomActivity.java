package com.shinow.qrscan;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.camera.view.PreviewView;
import androidx.fragment.app.Fragment;

import com.google.zxing.Result;
import com.king.zxing.CameraScan;
import com.king.zxing.DecodeConfig;
import com.king.zxing.DecodeFormatManager;
import com.king.zxing.DefaultCameraScan;
import com.king.zxing.ViewfinderView;
import com.king.zxing.analyze.MultiFormatAnalyzer;
import com.king.zxing.util.CodeUtils;
import com.king.zxing.util.LogUtils;
import com.shinow.qrscan.util.UriUtils;

import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;

import static androidx.camera.core.CameraX.getContext;

/**
 * 自定义扫码：当直接使用CaptureActivity
 * 自定义扫码，切记自定义扫码需在{@link Activity}或者{@link Fragment}相对应的生命周期里面调用{@link #mCameraScan}对应的生命周期
 *
 * @author <a href="mailto:jenly1314@gmail.com">Jenly</a>
 */
public class CustomActivity extends AppCompatActivity implements CameraScan.OnScanResultCallback {

    private CameraScan mCameraScan;

    private PreviewView previewView;

    private ViewfinderView viewfinderView;

    private View backLayout;
    private View lightLayout;
    private View photoLayout;
    private View selfLayout;
    private TextView lightTextView;

    private Toast toast;

    public static final int RC_CAMERA = 0X01;
    public static final int RC_READ_PHOTO = 0X02;
    public static final int REQUEST_CODE_PHOTO = 0X02;

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        setContentView(R.layout.activity_second);

        checkCameraPermissions();
    }

    private void initUI() {
        boolean isShowSelf = getIntent().getBooleanExtra("isShowSelf", false);
        previewView = findViewById(R.id.previewView);
        viewfinderView = findViewById(R.id.viewfinderView);
        lightLayout = findViewById(R.id.ivFlashlight);
        backLayout = findViewById(R.id.scan_back);
        photoLayout = findViewById(R.id.choose_photo);
        selfLayout = findViewById(R.id.choose_self);
        lightTextView = findViewById(R.id.txt_light);

        selfLayout.setVisibility(isShowSelf ? View.VISIBLE : View.GONE);
        //初始化解码配置
        DecodeConfig decodeConfig = new DecodeConfig();
        decodeConfig.setHints(DecodeFormatManager.DEFAULT_HINTS)//如果只有识别二维码的需求，这样设置效率会更高，不设置默认为DecodeFormatManager.DEFAULT_HINTS
                .setFullAreaScan(false)//设置是否全区域识别，默认false
                .setAreaRectRatio(0.8f)//设置识别区域比例，默认0.8，设置的比例最终会在预览区域裁剪基于此比例的一个矩形进行扫码识别
                .setAreaRectVerticalOffset(0)//设置识别区域垂直方向偏移量，默认为0，为0表示居中，可以为负数
                .setAreaRectHorizontalOffset(0);//设置识别区域水平方向偏移量，默认为0，为0表示居中，可以为负数
        mCameraScan = new DefaultCameraScan(this, previewView);

        //在启动预览之前，设置分析器，只识别二维码
        mCameraScan
                .setVibrate(true)//设置是否震动，默认为false
                .setNeedAutoZoom(true)//二维码太小时可自动缩放，默认为false
                .setAnalyzer(new MultiFormatAnalyzer(decodeConfig))
                .startCamera();//设置分析器,如果内置实现的一些分析器不满足您的需求，你也可以自定义去实现
        backLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });
        selfLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent resultIntent = new Intent();
                Bundle bundle = new Bundle();
                bundle.putString(CameraScan.SCAN_RESULT, "MY_QR_CODE");
                resultIntent.putExtras(bundle);
                setResult(RESULT_OK, resultIntent);
                finish();
            }
        });
        photoLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                checkExternalStoragePermissions();
            }
        });
        lightLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                toggleTorchState();
            }
        });
    }

    @Override
    protected void onDestroy() {
        mCameraScan.release();
        super.onDestroy();
    }

    /**
     * 切换闪光灯状态（开启/关闭）
     */
    protected void toggleTorchState() {
        if (mCameraScan != null) {
            boolean isTorch = mCameraScan.isTorchEnabled();
            mCameraScan.enableTorch(!isTorch);
            if (lightLayout != null) {
                lightLayout.setSelected(!isTorch);
                lightTextView.setText(isTorch ? "轻点关闭" : "轻点照亮");
            }
        }
    }

    /**
     * 扫码结果回调
     *
     * @param result 扫码结果
     * @return
     */
    @Override
    public boolean onScanResultCallback(Result result) {
//        Toast.makeText(CustomActivity.this, result.getText(), Toast.LENGTH_SHORT).show();
//            Intent resultIntent = new Intent();
//            Bundle bundle = new Bundle();
//            bundle.putString(CameraScan.SCAN_RESULT, result.getText());
//            resultIntent.putExtras(bundle);
//            setResult(RESULT_OK, resultIntent);
//            Toast.makeText(CustomActivity.this, result.getText(), Toast.LENGTH_SHORT).show();
//            finish();
        return false;
    }

    @AfterPermissionGranted(RC_READ_PHOTO)
    private void checkExternalStoragePermissions() {
        String[] perms = {Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE};
        if (EasyPermissions.hasPermissions(this, perms)) {//有权限
            startPhotoCode();
        } else {
            EasyPermissions.requestPermissions(this, "请允许获取使用相册权限",
                    RC_READ_PHOTO, perms);
        }
    }

    /**
     * 检测拍摄权限
     */
    @AfterPermissionGranted(RC_CAMERA)
    private void checkCameraPermissions() {
        String[] perms = {Manifest.permission.CAMERA};
        if (EasyPermissions.hasPermissions(this, perms)) {//有权限
            initUI();
        } else {
            finish();
        }
    }

    private void startPhotoCode() {
        Intent pickIntent = new Intent(Intent.ACTION_PICK,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        pickIntent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
        startActivityForResult(pickIntent, REQUEST_CODE_PHOTO);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && data != null) {
            switch (requestCode) {
//                case REQUEST_CODE_SCAN:
//                    String result = CameraScan.parseScanResult(data);
//                    showToast(result);
//                    break;
                case REQUEST_CODE_PHOTO:
                    parsePhoto(data);
                    break;
            }

        }
    }

    private void parsePhoto(Intent data) {
        final String path = UriUtils.getImagePath(this, data);
        LogUtils.d("path:" + path);
        if (TextUtils.isEmpty(path)) {
            Intent resultIntent = new Intent();
            Bundle bundle = new Bundle();
            bundle.putString(CameraScan.SCAN_RESULT, "");
            resultIntent.putExtras(bundle);
            setResult(RESULT_OK, resultIntent);
            finish();
            return;
        }
        //异步解析
        asyncThread(new Runnable() {
            @Override
            public void run() {
                final String result = CodeUtils.parseCode(path);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Intent resultIntent = new Intent();
                        Bundle bundle = new Bundle();
                        bundle.putString(CameraScan.SCAN_RESULT, result);
                        resultIntent.putExtras(bundle);
                        setResult(RESULT_OK, resultIntent);
                        finish();
                        Log.d("Jenly", "result:" + result);
//                        Toast.makeText(CustomActivity.this, result, Toast.LENGTH_SHORT).show();
                    }
                });

            }
        });

    }

    private void asyncThread(Runnable runnable) {
        new Thread(runnable).start();
    }
}