package com.example.bootservicefor7to9;

import androidx.activity.ComponentActivity;
import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Service;
import android.app.admin.DeviceAdminInfo;
import android.app.admin.DevicePolicyManager;
import android.app.backup.BackupAgentHelper;
import android.app.backup.SharedPreferencesBackupHelper;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.os.UserManager;
import android.provider.Settings;
import android.util.Log;
import android.content.BroadcastReceiver;
import android.widget.TextView;
import android.widget.Toast;

import java.io.DataInputStream;
import java.io.FileInputStream;
import java.util.Set;

import pub.devrel.easypermissions.EasyPermissions;

public class MainActivity extends ComponentActivity {

    boolean getAllpermission;
    private  DevicePolicyManager devicePolicyManager;
    private SharedPreferences sharedPreferences;

    public String SN;
    public boolean First = true;
    public final static int REQUEST_READ_PHONE_STATE = 1;


    TextView device;
    TextView os;
    TextView publicIP;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.device_data);

        device = findViewById(R.id.textView2);
        os = findViewById(R.id.textView3);
        publicIP = findViewById(R.id.textView4);

        devicePolicyManager = (DevicePolicyManager)getSystemService(Context.DEVICE_POLICY_SERVICE);
        sharedPreferences = getSharedPreferences("SN", 0);
        SN = sharedPreferences.getString("id", "");

        Log.e("SN",sharedPreferences.getAll().toString());
        Log.e("SN",SN);

        //檢查是否為初次登記
        if(SN.equals("") || SN.isEmpty())
        {
            First = true;
        }
        else
        {
            First = false;
        }
        //先打開取SN碼權限的操作
        int permissionCheck = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE);
        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.READ_PHONE_STATE}, 10);
           // Intent intent = new Intent(Manifest.permission.READ_PHONE_STATE);
            // ActivityCompat.requestPermissions(MainActivity.this, new String[]{"Settings.ACTION_MANAGE_OVERLAY_PERMISSION"},10);
            //startActivity(intent);
           /* try {
                if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_PHONE_STATE)) {//用户选择了禁止不再询问
                    final AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                    builder.setTitle("权限申请")
                            .setMessage("点击允许才可以使用我们的app哦")
                            .setPositiveButton("去允许", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface mDialog, int id) {
                                    ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.READ_PHONE_STATE}, 10);
                                }
                            });
                    builder.show();

                }
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }*/
        }


    }

    public class MyPrefsBackupAgent extends BackupAgentHelper {
        // The name of the SharedPreferences file
        static final String PREFS = "SN";

        // A key to uniquely identify the set of backup data
        static final String PREFS_BACKUP_KEY = "SN";

        // Allocate a helper and add it to the backup agent
        @Override
        public void onCreate() {
            SharedPreferencesBackupHelper helper =
                    new SharedPreferencesBackupHelper(this, PREFS);
            addHelper(PREFS_BACKUP_KEY, helper);
        }
    }
    @Override
    public  void onStart(){
        super.onStart();
        Log.i("start","starting");
    }
    @Override
    public  void onRestart()
    {
        super.onRestart();
    }
    @Override
    public void onPause() {
        // The service is no longer used and is being destroyed
        super.onPause();
        Log.v("MainActivity", "onPause");
    }
    @Override
    public void onResume() {

        super.onResume();

        Log.v("MainActivity", "onResume");

        int permissionCheck = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE);
        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {

            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_PHONE_STATE)) {//用户选择了禁止不再询问
                final AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setTitle("权限申请")
                        .setMessage("点击允许才可以使用我们的app哦")
                        .setPositiveButton("去允许", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface mDialog, int id) {
                                ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.READ_PHONE_STATE}, 10);
                            }
                        });
                builder.show();

            }
            else
            {
                final AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setTitle("权限申请")
                        .setMessage("需要打開權限才可以使用我们的app哦，請在setting中找到本app並打開權限頁面開啟所有要求權限")
                        .setPositiveButton("去允许", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface mDialog, int id) {
                                finish();
                            }
                        });
                builder.show();
            }

        } else {
            if(Build.VERSION.SDK_INT < Build.VERSION_CODES.O)
            {
                SN = Build.SERIAL;
            }
            else{
                SN = Build.getSerial();
            }
            Log.e("test",SN);
            if (First == false)
            {

                if (Settings.canDrawOverlays(MainActivity.this)) {

                    ComponentName componentName = new ComponentName(MainActivity.this, MyAdmin.class);
                    boolean isActive = devicePolicyManager.isAdminActive(componentName);
                    //檢查管理員權限
                    if (!isActive) {
                        Log.v("MainActivity", "get lock");
                        //要求打開管理員權限
                        Intent intent = new Intent();
                        intent.setAction(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN);
                        intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, componentName);
                        intent.putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION,
                                "You need to be a device admin to enable device admin.");

                        startActivity(intent);


                    } else {
                        //拿到所有需要申請的權限，就開始背景執行service
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            startForegroundService(new Intent(this, ServerService.class));
                        }
                        else
                        {
                            //ContextCompat.startForegroundService(this,ServerService.class);
                            startService(new Intent(this, ServerService.class));
                        }
                        //Log.e("start service","Test success");
                        //建立展示data View
                        String userDeviceName = Settings.Global.getString(getContentResolver(), Settings.Global.DEVICE_NAME);
                        if(userDeviceName == null)
                            userDeviceName = Settings.Secure.getString(getContentResolver(), "bluetooth_name");
                        device.setText("載具名稱:"+userDeviceName);
                        switch(Build.VERSION.SDK_INT) {
                            case 24:
                                os.setText("作業系統:" + "Android 7");
                                break;
                            case 25:
                                os.setText("作業系統:" + "Android 7.1");
                                break;

                            case 26:
                                os.setText("作業系統:" + "Android 8");
                                break;
                            case 27:
                                os.setText("作業系統:" + "Android 8.1");
                                break;
                            case 28:
                                os.setText("作業系統:" + "Android 9");
                                break;
                            default:
                                os.setText("作業系統:" + "未支援版本");
                                break;
                        }

                        publicIP.setText("Public IP:"+sharedPreferences.getString("pubIP","抓取中"));
                        //finish();
                    }
                } else {
                    //要求打開懸浮窗權限
                    Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION);
                    // ActivityCompat.requestPermissions(MainActivity.this, new String[]{"Settings.ACTION_MANAGE_OVERLAY_PERMISSION"},10);
                    startActivity(intent);
                    // finish();
                }

            }
            else
            {

                //打開登記SN碼頁面，進行上傳SN碼並取得連接資訊的動作
                Intent intent = new Intent(this, FirstActivity.class);
                someActivityResultLauncher.launch(intent);
            }
        }

    }


    ActivityResultLauncher<Intent> someActivityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        Intent intent = result.getData();
                        SN = intent.getStringExtra("SN");
                        if(!SN.isEmpty())
                        {
                            Log.e("SN","SN = " + SN);
                        }
                        //把連接資訊儲存在app中
                        String socketaddress = intent.getStringExtra("socketaddress");
                        String apiaddress = intent.getStringExtra("apiaddress");
                        String unitinfro = intent.getStringExtra("unitinfro");
                        Log.e("Http information",socketaddress);
                        Log.e("Http information",apiaddress);
                        Log.e("Http information",unitinfro);
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.putString("id",SN).putString("socketaddress",socketaddress).putString("apiaddress",apiaddress).putString("unitinfro",unitinfro).commit();

                        //登記完成，修改bool，讓Agent能開始背景執行
                        First = false;
                        //do a post api to  get socket connect data,also save into SharedPreferences(use for lost)

                    }
                    finishActivity(result.getResultCode());
                }
            });
    ActivityResultLauncher<Intent> somepermissionLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == Activity.RESULT_OK) {

                    }
                    finishActivity(result.getResultCode());
                }
            });
    @Override
    public void onDestroy() {
        // The service is no longer used and is being destroyed

        super.onDestroy();

        Log.v("MainActivity", "onDestroy");
    }
}