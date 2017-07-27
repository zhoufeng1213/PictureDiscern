package cn.btzh.shibiepicturewenzi;

import java.io.File;
import java.io.FileNotFoundException;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.Toast;

import com.googlecode.tesseract.android.TessBaseAPI;
import com.umeng.analytics.MobclickAgent;

import cn.btzh.shibiepicturewenzi.util.CheckPermession;
import cn.btzh.shibiepicturewenzi.util.ImgPretreatment;
import cn.btzh.shibiepicturewenzi.util.UnZipUtil;


public class MainActivity extends Activity implements OnClickListener {

    private static final int PHOTO_CAPTURE = 0x11;//
    private static final int PHOTO_RESULT = 0x12;//
    private final int SHOWRESULT = 0x101;
    private final int SHOWTREATEDIMG = 0x102;
    private static String LANGUAGE = "chi_sim";
    private String IMG_PATH;

    private ImageView ivSelected;
    private Button btnCamera;
    private Button btnSelect;
    private RadioGroup radioGroup;
    private EditText resultEditText;
    private String textResult;
    private Bitmap bitmapSelected;
    private Bitmap bitmapTreated;
    private View decodeProgressView;

    private TessBaseAPI baseApi;

    public Handler mHandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case SHOWRESULT:
                    decodeProgressView.setVisibility(View.GONE);
                    if (textResult.equals("")) {
                        resultEditText.setText("识别失败");
                    } else {
                        resultEditText.setEnabled(true);
                        resultEditText.setText(textResult);
                    }
                    break;
                case SHOWTREATEDIMG:
                    resultEditText.setEnabled(true);
                    resultEditText.setText("识别中......");
                    ivSelected.setImageBitmap(bitmapSelected);
                    break;
            }
            super.handleMessage(msg);
        }

    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.orc_layout);

        //检测权限
        if (Build.VERSION.SDK_INT >= 23) {
            CheckPermession.verifyCameraPermissions(this);
        } else {
            initOrMkFile();
        }
        initView();
    }

    private void initOrMkFile() {
        //把assets文件夹中的文件复制到手机的缓存中
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                boolean flag = UnZipUtil.unzipFile(MainActivity.this);
                Log.e("Unzip", "flag = " + flag);
            }
        }, 2000);

        IMG_PATH = getSDPath() + java.io.File.separator
                + "ocrtest";
        File path = new File(IMG_PATH);
        if (!path.exists()) {
            path.mkdirs();
        }
    }

    private void initView() {
        baseApi = new TessBaseAPI();
        ivSelected = (ImageView) findViewById(R.id.iv_selected);
        btnCamera = (Button) findViewById(R.id.btn_camera);
        btnSelect = (Button) findViewById(R.id.btn_select);
        radioGroup = (RadioGroup) findViewById(R.id.radiogroup);
        resultEditText = (EditText) findViewById(R.id.result);
        btnCamera.setOnClickListener(this);
        btnSelect.setOnClickListener(this);
        decodeProgressView = (View) findViewById(R.id.decodeView);
        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (checkedId) {
                    case R.id.rb_en:
                        LANGUAGE = "eng";
                        break;
                    case R.id.rb_ch:
                        LANGUAGE = "chi_sim";
                        break;
                }
            }

        });

        ivSelected.setFocusable(true);
        ivSelected.setFocusableInTouchMode(true);
        ivSelected.requestFocus();
        ivSelected.requestFocusFromTouch();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case CheckPermession.REQUEST_EXTERNAL_CAMEAR:
                boolean isGrant = true;
                for (int result : grantResults) {
                    if (result != PackageManager.PERMISSION_GRANTED) {
                        isGrant = false;
                    }
                }
                if (!isGrant) {
                    Toast.makeText(MainActivity.this, "权限获取失败，无法使用语音", Toast.LENGTH_SHORT).show();
                } else {
                    initOrMkFile();
                }
                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_CANCELED)
            return;

        if (requestCode == PHOTO_CAPTURE) {
            startPhotoCrop(Uri.fromFile(new File(IMG_PATH, "temp.jpg")));
        }

        if (requestCode == PHOTO_RESULT) {
            bitmapSelected = decodeUriAsBitmap(Uri.fromFile(new File(IMG_PATH,
                    "temp_cropped.jpg")));

            showPicture(ivSelected, bitmapSelected);
            decodeProgressView.setVisibility(View.VISIBLE);
            new Thread(new Runnable() {
                @Override
                public void run() {
                    bitmapTreated = ImgPretreatment
                            .converyToGrayImg(bitmapSelected);
                    Message msg = new Message();
                    msg.what = SHOWTREATEDIMG;
                    mHandler.sendMessage(msg);
                    textResult = doOcr(bitmapTreated, LANGUAGE);
                    Message msg2 = new Message();
                    msg2.what = SHOWRESULT;
                    mHandler.sendMessage(msg2);
                }

            }).start();

        }
    }

    @Override
    public void onClick(View v) {
        if (Build.VERSION.SDK_INT >= 23) {
            CheckPermession.verifyCameraPermissions(this);
        }
        Intent intent;
        switch (v.getId()) {
            case R.id.btn_camera:
                intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                intent.putExtra(MediaStore.EXTRA_OUTPUT,
                        Uri.fromFile(new File(IMG_PATH, "temp.jpg")));
                startActivityForResult(intent, PHOTO_CAPTURE);
                break;
            case R.id.btn_select:
                intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                intent.setType("image/*");
                intent.putExtra("crop", "true");
                intent.putExtra("scale", true);
                intent.putExtra("return-data", false);
                intent.putExtra(MediaStore.EXTRA_OUTPUT,
                        Uri.fromFile(new File(IMG_PATH, "temp_cropped.jpg")));
                intent.putExtra("outputFormat",
                        Bitmap.CompressFormat.JPEG.toString());
                intent.putExtra("noFaceDetection", true); // no face detection
                startActivityForResult(intent, PHOTO_RESULT);
                break;
        }
    }


    public static void showPicture(ImageView iv, Bitmap bmp) {
        iv.setImageBitmap(bmp);
    }


    /**
     * 识别图片上的文字
     *
     * @param bitmap
     * @param language
     * @return
     */
    public String doOcr(Bitmap bitmap, String language) {
        String path = UnZipUtil.dstPath(getApplication()) + File.separator;
        baseApi.init(path, language);

        bitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true);

        baseApi.setImage(bitmap);

        String text = baseApi.getUTF8Text();

        baseApi.clear();
        baseApi.end();

        return text;
    }


    public String getSDPath() {
        return UnZipUtil.dstPath(MainActivity.this);
    }


    /**
     * 开启图片裁剪
     *
     * @param uri
     */
    public void startPhotoCrop(Uri uri) {
        Intent intent = new Intent("com.android.camera.action.CROP");
        intent.setDataAndType(uri, "image/*");
        intent.putExtra("crop", "true");
        intent.putExtra("scale", true);
        intent.putExtra(MediaStore.EXTRA_OUTPUT,
                Uri.fromFile(new File(IMG_PATH, "temp_cropped.jpg")));
        intent.putExtra("return-data", false);
        intent.putExtra("outputFormat", Bitmap.CompressFormat.JPEG.toString());
        intent.putExtra("noFaceDetection", true); // no face detection
        startActivityForResult(intent, PHOTO_RESULT);
    }


    private Bitmap decodeUriAsBitmap(Uri uri) {
        Bitmap bitmap = null;
        try {
            bitmap = BitmapFactory.decodeStream(getContentResolver()
                    .openInputStream(uri));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return null;
        }
        return bitmap;
    }

    @Override
    protected void onResume() {
        super.onResume();
        MobclickAgent.onResume(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        MobclickAgent.onPause(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        baseApi.clear();
        baseApi.end();
    }
}
