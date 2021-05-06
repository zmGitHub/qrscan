package com.shinow.qrscan;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Vibrator;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.shinow.qrscan.util.CodeUtils;
import com.shinow.qrscan.util.UriUtils;
import com.shouzhong.scanner.Callback;
import com.shouzhong.scanner.IViewFinder;
import com.shouzhong.scanner.ScannerView;

import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;

/**
 *
 */
public class CustomActivity extends AppCompatActivity {

    private ScannerView scannerView;

    private View backLayout;
    private View lightLayout;
    private View photoLayout;
    private View selfLayout;
    private TextView lightTextView;

    private Toast toast;
    private Vibrator vibrator;

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
        scannerView = findViewById(R.id.previewView);
        lightLayout = findViewById(R.id.ivFlashlight);
        backLayout = findViewById(R.id.scan_back);
        photoLayout = findViewById(R.id.choose_photo);
        selfLayout = findViewById(R.id.choose_self);
        lightTextView = findViewById(R.id.txt_light);

        selfLayout.setVisibility(isShowSelf ? View.VISIBLE : View.GONE);
        scannerView.setShouldAdjustFocusArea(true);
        scannerView.setViewFinder(new ViewFinder(this));
        scannerView.setSaveBmp(false);
        scannerView.setRotateDegree90Recognition(true);
        scannerView.setEnableZXing(true);
        scannerView.setEnableZBar(true);
        scannerView.setEnableQrcode(true);
        scannerView.setEnableBarcode(true);
        scannerView.setCallback(new Callback() {
            @Override
            public void result(com.shouzhong.scanner.Result result) {
                Toast.makeText(CustomActivity.this, result.data, Toast.LENGTH_SHORT).show();
                startVibrator();
                Intent resultIntent = new Intent();
                Bundle bundle = new Bundle();
                bundle.putString(QrscanPlugin.SCAN_RESULT,  result.data);
                resultIntent.putExtras(bundle);
                setResult(RESULT_OK, resultIntent);
                finish();
//                tvResult.setText("识别结果：\n" + result.toString());
//                scannerView.restartPreviewAfterDelay(2000);
            }
        });
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
                bundle.putString(QrscanPlugin.SCAN_RESULT, "MY_QR_CODE");
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
    protected void onResume() {
        super.onResume();
        scannerView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        scannerView.onPause();
    }

    @Override
    protected void onDestroy() {
        if (vibrator != null) {
            vibrator.cancel();
            vibrator = null;
        }
        super.onDestroy();
    }

    private void startVibrator() {
        if (vibrator == null)
            vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        vibrator.vibrate(300);
    }

    /**
     * 切换闪光灯状态（开启/关闭）
     */
    protected void toggleTorchState() {
        if (scannerView != null) {
            scannerView.toggleFlash();
            boolean isFlashOn = scannerView.isFlashOn();
            if (lightLayout != null) {
                lightLayout.setSelected(isFlashOn);
                lightTextView.setText(!isFlashOn ? "轻点关闭" : "轻点照亮");
            }
        }
    }

//    /**
//     * 扫码结果回调
//     *
//     * @param result 扫码结果
//     * @return
//     */
//    @Override
//    public boolean onScanResultCallback(Result result) {
////        Toast.makeText(CustomActivity.this, result.getText(), Toast.LENGTH_SHORT).show();
////            Intent resultIntent = new Intent();
////            Bundle bundle = new Bundle();
////            bundle.putString(CameraScan.SCAN_RESULT, result.getText());
////            resultIntent.putExtras(bundle);
////            setResult(RESULT_OK, resultIntent);
////            Toast.makeText(CustomActivity.this, result.getText(), Toast.LENGTH_SHORT).show();
////            finish();
//        return false;
//    }

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
        Log.v("CustomActivity", "path:" + path);
        if (TextUtils.isEmpty(path)) {
            Intent resultIntent = new Intent();
            Bundle bundle = new Bundle();
            bundle.putString(QrscanPlugin.SCAN_RESULT, "");
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
                        bundle.putString(QrscanPlugin.SCAN_RESULT, result);
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

    class ViewFinder extends View implements IViewFinder {
        private Rect framingRect;//扫码框所占区域
        private float widthRatio = 0.66f;//扫码框宽度占view总宽度的比例
        private float heightRatio = 0.8f;
        private float heightWidthRatio =1f;//扫码框的高宽比
        private int leftOffset = -1;//扫码框相对于左边的偏移量，若为负值，则扫码框会水平居中
        private int topOffset = -1;//扫码框相对于顶部的偏移量，若为负值，则扫码框会竖直居中

        private int laserColor = 0xaaFE6B06;// 扫描线颜色
        private int maskColor = 0x60000000;// 阴影颜色
        private int borderColor = 0xffFE6B06;// 边框颜色
        private int borderStrokeWidth = 12;// 边框宽度
        private int borderLineLength = 72;// 边框长度

        private Paint laserPaint;// 扫描线
        private Paint maskPaint;// 阴影遮盖画笔
        private Paint borderPaint;// 边框画笔

        private int position;

        public ViewFinder(Context context) {
            super(context);
            setWillNotDraw(false);//需要进行绘制
            laserPaint = new Paint();
            laserPaint.setColor(laserColor);
            laserPaint.setStyle(Paint.Style.FILL);
            laserPaint.setStrokeWidth(1.0f);
            maskPaint = new Paint();
            maskPaint.setColor(maskColor);
            borderPaint = new Paint();
            borderPaint.setColor(borderColor);
            borderPaint.setStyle(Paint.Style.STROKE);
            borderPaint.setStrokeWidth(borderStrokeWidth);
            borderPaint.setAntiAlias(true);
        }

        @Override
        protected void onSizeChanged(int xNew, int yNew, int xOld, int yOld) {
            updateFramingRect();
        }

        @Override
        public void onDraw(Canvas canvas) {
            if (getFramingRect() == null) {
                return;
            }
            drawViewFinderMask(canvas);
            drawViewFinderBorder(canvas);
            drawLaser(canvas);
        }

        private void drawLaser(Canvas canvas) {
            Rect framingRect = getFramingRect();
            int top = framingRect.top + 10 + position;
            canvas.drawRect(framingRect.left + 10, top, framingRect.right - 10, top + 5, laserPaint);
            position = framingRect.bottom - framingRect.top - 25 < position ? 0 : position + 2;
            //区域刷新
            postInvalidateDelayed(2, framingRect.left + 10, framingRect.top + 10, framingRect.right - 10, framingRect.bottom - 10);
        }

        /**
         * 绘制扫码框四周的阴影遮罩
         */
        private void drawViewFinderMask(Canvas canvas) {
            int width = canvas.getWidth();
            int height = canvas.getHeight();
            Rect framingRect = getFramingRect();
            canvas.drawRect(0, 0, width, framingRect.top, maskPaint);//扫码框顶部阴影
            canvas.drawRect(0, framingRect.top, framingRect.left, framingRect.bottom, maskPaint);//扫码框左边阴影
            canvas.drawRect(framingRect.right, framingRect.top, width, framingRect.bottom, maskPaint);//扫码框右边阴影
            canvas.drawRect(0, framingRect.bottom, width, height, maskPaint);//扫码框底部阴影
        }

        /**
         * 绘制扫码框的边框
         */
        private void drawViewFinderBorder(Canvas canvas) {
            Rect framingRect = getFramingRect();

            // Top-left corner
            Path path = new Path();
            path.moveTo(framingRect.left, framingRect.top + borderLineLength);
            path.lineTo(framingRect.left, framingRect.top);
            path.lineTo(framingRect.left + borderLineLength, framingRect.top);
            canvas.drawPath(path, borderPaint);

            // Top-right corner
            path.moveTo(framingRect.right, framingRect.top + borderLineLength);
            path.lineTo(framingRect.right, framingRect.top);
            path.lineTo(framingRect.right - borderLineLength, framingRect.top);
            canvas.drawPath(path, borderPaint);

            // Bottom-right corner
            path.moveTo(framingRect.right, framingRect.bottom - borderLineLength);
            path.lineTo(framingRect.right, framingRect.bottom);
            path.lineTo(framingRect.right - borderLineLength, framingRect.bottom);
            canvas.drawPath(path, borderPaint);

            // Bottom-left corner
            path.moveTo(framingRect.left, framingRect.bottom - borderLineLength);
            path.lineTo(framingRect.left, framingRect.bottom);
            path.lineTo(framingRect.left + borderLineLength, framingRect.bottom);
            canvas.drawPath(path, borderPaint);
        }

        /**
         * 设置framingRect的值（扫码框所占的区域）
         */
        private synchronized void updateFramingRect() {
            Point viewSize = new Point(getWidth(), getHeight());
            int width = getWidth() * 801 / 1080, height = getWidth() * 811 / 1080;
            width = (int) (getWidth() * widthRatio);
//            height = (int) (getHeight() * heightRatio);
            height = (int) (heightWidthRatio * width);

            int left, top;
            if (leftOffset < 0) {
                left = (viewSize.x - width) / 2;//水平居中
            } else {
                left = leftOffset;
            }
            if (topOffset < 0) {
                top = (viewSize.y - height) / 3;//竖直居中
            } else {
                top = topOffset;
            }
            framingRect = new Rect(left, top, left + width, top + height);
        }

        @Override
        public Rect getFramingRect() {
            return framingRect;
        }
    }

    class ViewFinder2 implements IViewFinder {
        @Override
        public Rect getFramingRect() {
            return new Rect(240, 240, 840, 840);
        }
    }
}