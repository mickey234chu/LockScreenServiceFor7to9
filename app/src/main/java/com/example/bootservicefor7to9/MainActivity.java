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
    public boolean closescreen = false;

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

        //???????????????????????????
        if(SN.equals("") || SN.isEmpty())
        {
            First = true;
        }
        else
        {
            First = false;
        }
        //????????????SN??????????????????
        int permissionCheck = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE);
        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.READ_PHONE_STATE}, 10);

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

            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_PHONE_STATE)) {//?????????????????????????????????
                final AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setTitle("????????????")
                        .setMessage("????????????????????????????????????app???")
                        .setPositiveButton("?????????", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface mDialog, int id) {
                                ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.READ_PHONE_STATE}, 10);
                            }
                        });

                    builder.show();



            }
            else
            {
                final AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setTitle("????????????")
                        .setMessage("??????????????????????????????????????????app????????????setting????????????app?????????????????????????????????????????????")
                        .setPositiveButton("?????????", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface mDialog, int id) {
                                closescreen = true;
                                finish();
                            }
                        });
                //???????????????onresume ??????2???????????????2???????????????????????????
                if(!closescreen ) {
                    builder.show();
                }

            }

        }
        else {
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
                    //?????????????????????
                    if (!isActive) {
                        Log.v("MainActivity", "get lock");
                        //???????????????????????????
                        Intent intent = new Intent();
                        intent.setAction(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN);
                        intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, componentName);
                        intent.putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION,
                                "You need to be a device admin to enable device admin.");

                        startActivity(intent);


                    } else {
                        //?????????????????????????????????????????????????????????service
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            startForegroundService(new Intent(this, ServerService.class));
                        }
                        else
                        {
                            //ContextCompat.startForegroundService(this,ServerService.class);
                            startService(new Intent(this, ServerService.class));
                        }
                        //Log.e("start service","Test success");
                        //????????????data View
                        String userDeviceName = Settings.Global.getString(getContentResolver(), Settings.Global.DEVICE_NAME);
                        if(userDeviceName == null)
                            userDeviceName = Settings.Secure.getString(getContentResolver(), "bluetooth_name");
                        device.setText("????????????:"+userDeviceName);
                        switch(Build.VERSION.SDK_INT) {
                            case 24:
                                os.setText("????????????:" + "Android 7");
                                break;
                            case 25:
                                os.setText("????????????:" + "Android 7.1");
                                break;

                            case 26:
                                os.setText("????????????:" + "Android 8");
                                break;
                            case 27:
                                os.setText("????????????:" + "Android 8.1");
                                break;
                            case 28:
                                os.setText("????????????:" + "Android 9");
                                break;
                            default:
                                os.setText("????????????:" + "???????????????");
                                break;
                        }

                        publicIP.setText("Public IP:"+sharedPreferences.getString("pubIP","?????????"));
                        //finish();
                    }
                } else {
                    //???????????????????????????
                    Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION);
                    // ActivityCompat.requestPermissions(MainActivity.this, new String[]{"Settings.ACTION_MANAGE_OVERLAY_PERMISSION"},10);
                    startActivity(intent);
                    // finish();
                }

            }
            else
            {

                //????????????SN????????????????????????SN?????????????????????????????????
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
                        //????????????????????????app???
                        String socketaddress = intent.getStringExtra("socketaddress");
                        String apiaddress = intent.getStringExtra("apiaddress");
                        String unitinfro = intent.getStringExtra("unitinfro");
                        Log.e("Http information",socketaddress);
                        Log.e("Http information",apiaddress);
                        Log.e("Http information",unitinfro);
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.putString("id",SN).putString("socketaddress",socketaddress).putString("apiaddress",apiaddress).putString("unitinfro",unitinfro).commit();

                        //?????????????????????bool??????Agent?????????????????????
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