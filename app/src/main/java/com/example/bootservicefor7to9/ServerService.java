package com.example.bootservicefor7to9;


import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.app.admin.DevicePolicyManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Insets;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.provider.Settings;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowInsets;
import android.view.WindowInsetsController;
import android.view.WindowManager;
import android.view.WindowMetrics;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBar;
import androidx.core.app.NotificationCompat;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.apache.log4j.lf5.util.Resource;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.drafts.Draft_6455;
import org.java_websocket.handshake.ServerHandshake;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedWriter;
import java.io.IOError;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.TimeZone;
import java.util.Timer;
import java.util.TimerTask;

public class ServerService<Myboolean> extends Service {



    IBinder mBinder;      // interface for clients that bind
    boolean mAllowRebind; // indicates whether onRebind should be used
    boolean socketest = false;
    boolean functiontest = false;
    boolean issocketchange = false;
    boolean changesocket = false;
    static boolean working= false;
    private boolean Block_Flag = false;
    private boolean Lock_Flag = false;
    private boolean Lost_Flag = false;
    private MyWebSocketClient websocket;
    private Thread Thread1 = null;
    private static Thread Thread2 = null;

    private String pubIP;

    public static String owner;
    Timer timer = new Timer();
    Timer locktimer = new Timer();

    TimerTask task ;
    TimerTask locktask;

    //window setting
    private View floatView;
    private TextView screentext;

    //Lockscreen Manager,???????????????????????????
    private WindowManager.LayoutParams floatWindowLayoutParam;
    private WindowManager windowManager;
    private WindowInsetsController controller;
    private WindowInsetsController controller2;
    private String LockMessage;
    private String LostMessage;

    //blockinput ???????????????
    private WindowManager.LayoutParams params;
    private WindowManager lockManager;
    private MoniterView mMoniterView;
    private GestureDetector mGestureDetector;
    //????????????
    private DevicePolicyManager devicePolicyManager;

    //?????????
    private int StartLock = 0;
    private int StopLock = 0;
    private boolean timelock = false;
    private Calendar Gettime;
    private final SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
    private ComponentName compName;
    private AlarmReceiver mAlarmBroadcastReceiver;

    private HomeKeyBroadcastReceiver mHomeKeyBroadcastReceiver;
    //SN
    private SharedPreferences sharedPreferences;
    public static String ID;


    HttpURLConnection connection;
    private String apiaddress;
    //api address
    URL url;
    //?????? ip
    URL ipurl = new URL("https://api.ipify.org?format=json");
    //websocket
    String websocketLink;
    //URI uri;
    public ServerService() throws MalformedURLException {
    }











    @Override
    public void onCreate()
    {
        // The service is being created
        super.onCreate();

        Log.v("TestService","start");
        //API > 26
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForeground(1,getNotification());
        }
        else
        {
            Notification.Builder builder = new Notification.Builder(this);
            builder.setSmallIcon(R.mipmap.ic_launcher);
            builder.setContentTitle("Service start");
            builder.setContentText("Service is start");
            startForeground(1, builder.build());
        }
        Log.i("TestService","startForeground");




    }

    //?????????
    private void initViews() throws IOException {
        //channel = new NotificationChannel("testopen", "test", NotificationManager.IMPORTANCE_HIGH);
        httpCall(ipurl.toString());
        //?????????SN
        sharedPreferences = getSharedPreferences("SN", 0);
        ID = sharedPreferences.getString("id","");

        //???websocket link
        if(socketest)
        {
            //???????????????websocket
            websocketLink = "ws://imoeedge20220914134800.azurewebsites.net/api/WebSoket?nickName="+ID;
            //uri = URI.create("ws://imoeedge20220914134800.azurewebsites.net/api/WebSoket?nickName="+ID);
            Log.e("test link",websocketLink);
        }
        else
        {
            //?????????????????????????????????websocket
            String socketaddress = sharedPreferences.getString("socketaddress","");
            String currentsocket = sharedPreferences.getString("currentsocket","");
            if(currentsocket.equals("")||socketaddress == currentsocket)
            {
                websocketLink = socketaddress +ID;
            }
            else
            {
                websocketLink = currentsocket+ID ;
            }

            //uri = URI.create(websocketLink+ID);
            Log.e("not test link ",websocketLink);
        }
        apiaddress = sharedPreferences.getString("apiaddress","");
        //?????????????????????API?????????
        if(socketest)
        {
            url = new URL("http://imoeedge20220914134800.azurewebsites.net/api/UserTime");
            Log.e("apiaddress","http://imoeedge20220914134800.azurewebsites.net/api/UserTime");


        }
        else
        {
            url = new URL(apiaddress.trim());
            Log.e("apiaddress",sharedPreferences.getString("apiaddress","null"));
        }

        //???????????????
        mAlarmBroadcastReceiver = new AlarmReceiver();
        IntentFilter mAlarmIntentFilter = new IntentFilter();
        mAlarmIntentFilter.addAction("activity_app");
        registerReceiver(mAlarmBroadcastReceiver,mAlarmIntentFilter);
        StartLock = sharedPreferences.getInt("Starttime",-1);
        StopLock = sharedPreferences.getInt("Endtime",-1);

        //???????????????????????????????????????
        owner = sharedPreferences.getString("unitinfro","");
        LostMessage = owner +"\n????????????";
        LockMessage = "";
        Log.e("unitinfro",sharedPreferences.getString("unitinfro","null"));


        if(timerlockhandle(StartLock,StopLock))
        {
            timelock = true;
        }

        //??????lock flag,lost flag ????????????????????????????????????
        Lock_Flag = sharedPreferences.getBoolean("lock",false);
        Lost_Flag = sharedPreferences.getBoolean("lost",false);

        //?????????device Manager???????????????lock now ????????????
        mGestureDetector = new GestureDetector(null, new MyGestureDetectorListener(),null);
        devicePolicyManager = (DevicePolicyManager) getSystemService(DEVICE_POLICY_SERVICE);
        compName = new ComponentName(this, MyAdmin.class);

        //????????????sendata
        Calendar cal = new GregorianCalendar(TimeZone.getTimeZone("Asia/Taipei")); //????????????
        cal.add(Calendar.MINUTE, 1);
        add_alarm(ServerService.this,cal,"send_data",0);


        //????????????socket
        Thread1 = new Thread(new Thread1());
    }
    //??????????????????????????????????????????
    private static class MyGestureDetectorListener implements GestureDetector.OnGestureListener {
        @Override
        public boolean onDown(MotionEvent motionEvent) {
            Log.e("Listen","onDown");
            //devicePolicyManager.lockNow();
            return false;
        }

        @Override
        public void onShowPress(MotionEvent motionEvent) {
            Log.e("Listen","onShowPress");
            //devicePolicyManager.lockNow();

        }

        @Override
        public boolean onSingleTapUp(MotionEvent motionEvent) {
            Log.e("Listen","onSingleTapUp");
            //devicePolicyManager.lockNow();
            return false;
        }

        @Override
        public boolean onScroll(MotionEvent motionEvent, MotionEvent motionEvent1, float v, float v1) {
            Log.e("Listen","onScroll");
            //devicePolicyManager.lockNow();
            return false;
        }

        @Override
        public void onLongPress(MotionEvent motionEvent) {
            Log.e("Listen","onLongPress");
           // devicePolicyManager.lockNow();

        }

        @Override
        public boolean onFling(MotionEvent motionEvent, MotionEvent motionEvent1, float v, float v1) {
            Log.e("Listen","onFling");
            //devicePolicyManager.lockNow();
            return false;
        }
    }
    //??????????????????????????????????????????
    private boolean timerlockhandle(int start,int end)
    {
        Gettime = Calendar.getInstance();
        //???????????????,???
        int hour = Gettime.get(Calendar.HOUR_OF_DAY);
        int minute = Gettime.get(Calendar.MINUTE);
        //????????????????????????????????????
        int minuteofDay = hour*60 + minute;

        if(minuteofDay == 0)
        {
            minuteofDay = 24*60;
        }
        //3??????????????????:
        // start ??? end ??? (???09:00-13:00) => ??? minuteofDay ???????????????????????????
        // start ??? end ??? (???23:00-01:00) => ??? minuteofDay>=start??????<=end???????????????//
        // start == end => minuteofDay == ?????????????????????//
        if(start < end)
        {
            return minuteofDay >= start && minuteofDay <= end;
        }
        else if(start > end)
        {
            return minuteofDay >= start || minuteofDay <= end;
        }
        else
        {
            return minuteofDay == start;
        }
    }
    //??????????????????(????????????)
    private  int timetoint(String time)
    {

        int hour = Integer.parseInt(time.split("[:]")[0]);
        int minutes = Integer.parseInt(time.split("[:]")[1]);
        int minutesofDay = hour*60 + minutes;
        Log.e("time",Integer.toString(minutesofDay));
        return minutesofDay;
    }

    //??????????????????
    static class Data
    {
        String publicip;
        String uploadtime;
        String serialnumber;
        String owner;
        public Data(String localIpAddress, String date, String id, String own) {

            publicip = localIpAddress;
            uploadtime = date;
            serialnumber = id;
            owner = own;
        }
    }



    //????????????Server?????????
    @RequiresApi(api = Build.VERSION_CODES.O)
    private Notification getNotification()
    {
        String ID = "com.example.bootservice";
        String NAME = "Channel ONE";
        Intent newintent = new Intent(ServerService.this,MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivities(this,0, new Intent[]{newintent},PendingIntent.FLAG_IMMUTABLE);
        NotificationCompat.Builder notification;
        NotificationManager manager = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
        NotificationChannel channel = new NotificationChannel(ID,NAME,manager.IMPORTANCE_HIGH);
        manager.createNotificationChannel(channel);
        notification = new NotificationCompat.Builder(ServerService.this,ID);
        notification.setContentTitle("Service start")
                .setContentText("Service is start")
        .setSmallIcon(R.drawable.ic_launcher_foreground)
        .setContentIntent(pendingIntent);

        return notification.build();
    }
    //?????????????????????
    private void setwindow( String text)
    {


        //get width and height

        int width ;
        int height ;
        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);

        DisplayMetrics metrics = new DisplayMetrics();
        metrics = getApplicationContext().getResources().getDisplayMetrics();
        width = metrics.widthPixels;
        height = metrics.heightPixels;

        if(Build.VERSION.SDK_INT>=30) {
            WindowMetrics metrics2 = windowManager.getCurrentWindowMetrics();
            WindowInsets windowInsets = metrics2.getWindowInsets();
            Insets insets = windowInsets.getInsetsIgnoringVisibility(WindowInsets.Type.navigationBars()|WindowInsets.Type.displayCutout());
            width= metrics2.getBounds().width()+ insets.right+insets.left;
            height=metrics2.getBounds().height()+insets.top+insets.bottom;

        }
        else
        {
            Resources resources = getApplicationContext().getResources();
            int resourceId = resources.getIdentifier("status_bar_height", "dimen", "android");
            height += resources.getDimensionPixelSize(resourceId);
            int resourceId2 = resources.getIdentifier("navigation_bar_height", "dimen", "android");
            height += resources.getDimensionPixelSize(resourceId2);
        }
        Log.i("Width,Height:",width +","+height);

        //xml -> view
        LayoutInflater inflater = (LayoutInflater) getBaseContext().getSystemService(LAYOUT_INFLATER_SERVICE);
        //get lockscreen layout

        floatView = (ViewGroup) inflater.inflate(R.layout.lockscreen, null);
        screentext = floatView.findViewWithTag("locktext");
        if(text.equals(""))
        {
            screentext.setText(LostMessage);
        }
        else
        {
            screentext.setText(text);
        }
        if(Lost_Flag)
        {
            screentext.setText(LostMessage);
        }

        floatView.setKeepScreenOn(true);
        floatView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION|View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
        floatView.setOnTouchListener((view, motionEvent) -> {
               Log.d("TestService","testtouch");
               return mGestureDetector.onTouchEvent(motionEvent);

           });

        int LAYOUT_TYPE;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            LAYOUT_TYPE = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        }else {
            LAYOUT_TYPE = WindowManager.LayoutParams.TYPE_PHONE;
        }

        floatWindowLayoutParam = new WindowManager.LayoutParams(
                (int) width,
                (int)height,
                LAYOUT_TYPE,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE ,
                PixelFormat.TRANSLUCENT
        );

        // The Gravity of the Floating Window is set.
        // The Window will appear in the center of the screen


        floatWindowLayoutParam.gravity = Gravity.CENTER;
        floatWindowLayoutParam.flags |= WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS;
        floatWindowLayoutParam.flags |= WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON;
        floatWindowLayoutParam.flags |= WindowManager.LayoutParams.FLAG_FULLSCREEN;
        floatWindowLayoutParam.flags |= WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL;
        floatWindowLayoutParam.flags |= WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN;
        // X and Y value of the window is set
        floatWindowLayoutParam.x = 0;
        floatWindowLayoutParam.y = 0;

        windowManager.addView(floatView, floatWindowLayoutParam);

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
            controller = floatView.getWindowInsetsController();
            controller.hide(android.view.WindowInsets.Type.statusBars()
                    | android.view.WindowInsets.Type.navigationBars());



        }
        else
        {

        }


    }
    //????????????????????????????????????
    private void setLockManager()
    {

        if(lockManager == null)
        {
            lockManager = (WindowManager)getSystemService(Context.WINDOW_SERVICE);
            mMoniterView = new MoniterView(ServerService.this);


        }

        int LAYOUT_TYPE;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            LAYOUT_TYPE = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        }else {
            LAYOUT_TYPE = WindowManager.LayoutParams.TYPE_PHONE;
        }
        if(null != lockManager)
        {
            params = new WindowManager.LayoutParams(
                    1,1,
                    LAYOUT_TYPE,
                    WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
                    PixelFormat.TRANSLUCENT
            );
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                params.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
            }
            else
            {
                params.type = WindowManager.LayoutParams.TYPE_SYSTEM_OVERLAY;
            }
            //params.type = WindowManager.LayoutParams.TYPE_KEYGUARD_DIALOG;
            params.format = PixelFormat.RGBA_8888;
            params.flags = WindowManager.LayoutParams.FLAG_FULLSCREEN;
        }
        mMoniterView.setFocusable(true);
        mMoniterView.setKeepScreenOn(true);
        mMoniterView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN|View.SYSTEM_UI_FLAG_FULLSCREEN|View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION|View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
        try
        {
            lockManager.addView(mMoniterView,params);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                controller2 = mMoniterView.getWindowInsetsController();
                controller2.hide(WindowInsets.Type.statusBars()
                        | WindowInsets.Type.navigationBars());



            }
            else {

            }

        }
        catch (IllegalArgumentException e )
        {
            e.printStackTrace();
        }

    }
    //?????????IP
    public static String PIP;
    public void httpCall(String url) {
        //RequestQueue initialized
        //publicip
        RequestQueue mRequestQueue = Volley.newRequestQueue(this);

        //String Request initialized
        StringRequest mStringRequest = new StringRequest(Request.Method.GET, url, response -> {
            Log.e("ip", response);
            pubIP = response;
            if(!pubIP.isEmpty())
            {
                pubIP= pubIP.replaceAll("\"","").replaceFirst("ip:","").replace("{","").replace("}","");
                PIP = pubIP;
                Log.e("IP",pubIP);
            }
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString("pubIP",pubIP).commit();
        }, error -> pubIP = "0.0.0.0");

        mRequestQueue.add(mStringRequest);
    }
    //??????statusBars
    public  class HomeKeyBroadcastReceiver extends BroadcastReceiver{

        @Override
        public void onReceive(Context context,Intent intent)
        {
            //devicePolicyManager.lockNow();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {

                controller.hide(WindowInsets.Type.statusBars()
                        | WindowInsets.Type.navigationBars());
            }
            else
            {

            }
        }
    }
    public static class AlarmReceiver extends BroadcastReceiver {
        HttpURLConnection Alarmconnection;
        URL SendUrl;
        {
            try {
                SendUrl = new URL("http://imoeedge20220914134800.azurewebsites.net/api/UserTime");
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
        }
        Calendar Gettime;
        String SendIP = PIP;
        @Override
        public void onReceive(Context context, Intent intent) {
            Bundle bData = intent.getExtras();
            if(bData.get("title").equals("timelock"))
            {
                //?????????????????????

                Log.i("add_Alarm_check","receive sucess, time = "+bData.get("time"));
            }
            if(bData.get("title").equals("send_data"))
            {
                //?????????????????????
                new Thread(new Thread2()).start();
                Log.i("add_Alarm_check","data send");
                Calendar cal = new GregorianCalendar(TimeZone.getTimeZone("Asia/Taipei")); //????????????
                cal.add(Calendar.MINUTE, 1);
                add_alarm(context,cal,"send_data",0);
                Log.i("add_Alarm_check","resenddata");
            }
            Log.i("add_Alarm_check","working");
        }
    }
    /***    ??????(???????????????)??????    ***/
    public static void add_alarm(Context context, Calendar cal,String title, int reqCode) {
        Log.d("add_Alarm_check", title + " alarm add time: " + String.valueOf(cal.get(Calendar.MONTH)) + "." + String.valueOf(cal.get(Calendar.DATE)) + " " + String.valueOf(cal.get(Calendar.HOUR_OF_DAY)) + ":" + cal.get(Calendar.MINUTE) + ":" + cal.get(Calendar.SECOND));

        Intent intent = new Intent(context, AlarmReceiver.class);
        // ?????????????????????????????? category ?????????????????????
        intent.addCategory("ID." + String.valueOf(cal.get(Calendar.MONTH)) + "." + String.valueOf(cal.get(Calendar.DATE)) + "-" + String.valueOf((cal.get(Calendar.HOUR_OF_DAY) )) + "." + String.valueOf(cal.get(Calendar.MINUTE)) + "." + String.valueOf(cal.get(Calendar.SECOND)));
        String AlarmTimeTag = "Alarmtime " + String.valueOf(cal.get(Calendar.HOUR_OF_DAY)) + ":" + String.valueOf(cal.get(Calendar.MINUTE)) + ":" + String.valueOf(cal.get(Calendar.SECOND));

        intent.putExtra("title", title);
        intent.putExtra("time", AlarmTimeTag);

        PendingIntent pi = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT| PendingIntent.FLAG_IMMUTABLE);

        AlarmManager am = (AlarmManager) context.getSystemService(ALARM_SERVICE);
        am.set(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), pi);       //????????????
    }
    private static void cancel_alarm(Context context, Calendar cal,String title,int reqCode) {
        Log.d("add_Alarm_check", "alarm cancel time: " + String.valueOf(cal.get(Calendar.MONTH)) + "." + String.valueOf(cal.get(Calendar.DATE)) + " " + String.valueOf(cal.get(Calendar.HOUR_OF_DAY)) + ":" + cal.get(Calendar.MINUTE) + ":" + cal.get(Calendar.SECOND));

        Intent intent = new Intent(context, AlarmReceiver.class);
        // ?????????????????????????????? category ?????????????????????
        intent.addCategory("ID." + String.valueOf(cal.get(Calendar.MONTH)) + "." + String.valueOf(cal.get(Calendar.DATE)) + "-" + String.valueOf((cal.get(Calendar.HOUR_OF_DAY) )) + "." + String.valueOf(cal.get(Calendar.MINUTE)) + "." + String.valueOf(cal.get(Calendar.SECOND)));
        String AlarmTimeTag = "Alarmtime " + String.valueOf(cal.get(Calendar.HOUR_OF_DAY)) + ":" + String.valueOf(cal.get(Calendar.MINUTE)) + ":" + String.valueOf(cal.get(Calendar.SECOND));

        intent.putExtra("title", title);
        intent.putExtra("time", AlarmTimeTag);

        PendingIntent pi = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT| PendingIntent.FLAG_IMMUTABLE);

        AlarmManager am = (AlarmManager) context.getSystemService(ALARM_SERVICE);
        am.cancel(pi);    //??????????????????????????????
    }

    //websocket
    public class MyWebSocketClient extends WebSocketClient
    {

        public MyWebSocketClient(URI serverUri) {
            super(serverUri,new Draft_6455());
            Log.e("websocket","oncreate,link = "+ serverUri.toString());
        }

        @Override
        public void onOpen(ServerHandshake handshakedata) {
           Log.e("websocket","onOpen");
           //websocket.send(ID+"connect");
        }

        @Override
        public void onMessage(String message) {
            if (message != null) {
                Log.e("MessageS",message);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                if(message.contains(ID))
                {
                    if(message.contains(ID+"_lock") ) {


                        String text = message.replaceFirst(ID+"_lock","").replaceFirst(":","");


                        if(text.length()>0)
                        {
                            handler.post(() -> {

                                LockMessage = text;
                                //screentext.setText(LockMessage);
                                Log.e("onMessage","savetext");
                            });
                        }
                        else
                        {
                            handler.post(() -> {
                                Lock_Flag = true;
                                Lost_Flag = true;
                                LockMessage = LostMessage;
                                //screentext.setText(LockMessage);
                                //LockMessage = LostMessage;
                            });

                        }
                        //editor.putString("message", text).commit();
                        try {
                            Log.i("TestService", "lockMessages");

                            if(Block_Flag )//?????????????????????????????????,false=?????????
                            {
                                handler.post(() -> {
                                    Log.e("text",Integer.toString(text.length()));
                                    screentext.setText(LockMessage);
                                    if(text.length()>0) {

                                        websocket.send(ID+"set text!");
                                    }
                                });
                            }
                            else
                            {
                                Lock_Flag = true;
                                websocket.send(ID+"lock!");
                            }

                        }
                        catch (IOError e)
                        {
                            e.printStackTrace();
                        }
                        editor.putBoolean("lock", Lock_Flag).putBoolean("lost",Lost_Flag).commit();

                    }
                    else if(message.contains(ID+"_time:")) //input format=>time:HH:mm,HH:mm
                    {
                        try {
                            Log.i("TestService", "timeMessages");


                            String getTime = message.replaceFirst(ID+"_time:","");
                            int length = getTime.split("[,]").length;
                            Log.e("TestService", Integer.toString(length));
                            StartLock = timetoint(getTime.split("[,]")[0]);
                            StopLock = timetoint (getTime.split("[,]")[1]);
                            //SharedPreferences.Editor editor = sharedPreferences.edit();
                            editor.putInt("Starttime",StartLock).putInt("Endtime",StopLock).commit();
                            Log.e("time",Integer.toString(StopLock));
                            websocket.send(ID+" set time!");


                        }
                        catch (IOError e)
                        {
                            e.printStackTrace();
                        }
                    }
                    else if(message.contains(ID+"_unlock") )
                    {
                        Lock_Flag = false;
                        Lost_Flag = false;
                        //SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.putBoolean("lock", Lock_Flag).putBoolean("lost",Lost_Flag).commit();
                        if(!timerlockhandle(StartLock,StopLock))
                        {
                            websocket.send(ID+" unlock!");
                        }

                    }
                    else if (message.contains(ID+"_timereset"))
                    {
                        //SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.putInt("Starttime",-1).putInt("Endtime",-1).commit();
                        StartLock = -1;
                        StopLock = -1;
                        timelock = false;
                        //?????????????????????????????????
                        if(Block_Flag)
                        {
                            windowManager.removeView(floatView);
                            lockManager.removeView(mMoniterView);
                            unregisterReceiver(mHomeKeyBroadcastReceiver);
                            Block_Flag = false;
                            Log.v("TestService","resettimer");

                        }
                        websocket.send(ID+" timereset!");
                    }
                    else if (message.contains(ID+"_switch:"))//????????????websocket
                    {
                        String newlink  = message.replaceFirst(ID+"_switch","").replaceFirst(":","");
                        editor.putString("currentsocket",newlink).commit();
                        websocketLink = newlink+ID;

                        //????????????
                        Log.e("websocket link switch to:",websocketLink);

                        //??????????????????websocket
                        changesocket = true;

                    }
                    else
                    {
                        String text = message.replaceFirst(ID,"");

                        if(!Block_Flag) {
                            handler.post(() -> {
                                Toast.makeText(getApplicationContext(),
                                        text, Toast.LENGTH_LONG).show();
                                websocket.send(ID + " message!");
                            });
                        }

                    }
                }




            } else {
                Log.e("MessageS","nothing");

            }
        }

        @Override
        public void onClose(int code, String reason, boolean remote) {
            Log.e("websocketclose",reason+"|"+code);

            //???????????????
            if(code == 1 || code == 1000)
            {

            }
            else
            {
                handler.post(() -> {
                    Toast.makeText(getApplicationContext(),
                            "websocket conn fail",Toast.LENGTH_LONG).show();

                });
            }


           // closeReceiveConnect();
        }

        @Override
        public void onError(Exception ex) {
            Log.e("websocketerror",ex.toString());
            //closeReceiveConnect();

        }
    }

    //????????? websocket
    public void initwebSocket()
    {

        if(null != websocket)
        {
            websocket = null;
        }
        //??????????????????uri????????????
        URI uri;
        uri = URI.create(websocketLink);
        Log.e("initwebsocket link:", uri.toString());
        websocket = new MyWebSocketClient(uri);
        try
        {

            websocket.connectBlocking();
            changesocket = false;

        }
        catch (InterruptedException e)
        {
            e.printStackTrace();
            Log.e("testwebsocket","testfail");

        }

    }
    //?????????
    private  static  final long HEART_BEAT_RATE = 60*1000;
    private final Handler mHandler = new Handler();
    //??????????????????
    private final Runnable LockFlagRunnable = new Runnable() {
        @Override
        public void run() {
            timelock = timerlockhandle(StartLock,StopLock);
            if(timelock)
            {
                if(!Block_Flag)//?????????????????????????????????,false=?????????
                {
                    if (Settings.canDrawOverlays(ServerService.this)) {
                        handler.post(() -> {
                            setLockManager();
                            setwindow(LostMessage);

                            IntentFilter mIntentFilter = new IntentFilter(Intent.ACTION_CLOSE_SYSTEM_DIALOGS);
                            mHomeKeyBroadcastReceiver = new HomeKeyBroadcastReceiver();
                            registerReceiver(mHomeKeyBroadcastReceiver, mIntentFilter);
                            Block_Flag=true;

                        });

                        Log.e("lock","lock");

                    } else {
                        Log.e("ERROR", "didn't get the permission");
                    }
                    Block_Flag = true;
                }
            }
            else if(Lock_Flag)
            {
                if(!Block_Flag)//?????????????????????????????????,false=?????????
                {
                    if (Settings.canDrawOverlays(ServerService.this)) {
                        handler.post(() -> {
                            setLockManager();
                            setwindow(LockMessage);

                            IntentFilter mIntentFilter = new IntentFilter(Intent.ACTION_CLOSE_SYSTEM_DIALOGS);
                            mHomeKeyBroadcastReceiver = new HomeKeyBroadcastReceiver();
                            registerReceiver(mHomeKeyBroadcastReceiver, mIntentFilter);
                            Block_Flag=true;

                        });

                        Log.e("lock","lock");

                    } else {
                        Log.e("ERROR", "didn't get the permission");
                    }
                    Block_Flag = true;
                }
            }
            else
            {
                if(Block_Flag)//?????????????????????????????????
                {
                    windowManager.removeView(floatView);
                    lockManager.removeView(mMoniterView);
                    unregisterReceiver(mHomeKeyBroadcastReceiver);
                    Block_Flag = false;
                    Log.v("TestService","unlock");


                }
            }
            //????????????websocket,?????????????????????websocket
            if(changesocket)
            {
                closeReceiveConnect();
                initwebSocket();
            }
            mHandler.postDelayed(this,2*1000);
        }
    };
    //????????????
    private final Runnable heartBeatRunnable = new Runnable() {
        @Override
        public void run() {
            if(websocket != null)
            {
                if (websocket.isClosed())
                {
                    //????????????websocket
                    reconnectWs();
                }
                else if (websocket.isOpen())
                {
                    websocket.send(" ");
                    Log.e("websocket","still alive");
                }
            }
            else
            {
                //websocket???????????????
                websocket= null;
                initwebSocket();
            }



            mHandler.postDelayed(this,HEART_BEAT_RATE);
        }
    };

    //???????????? websocket ???????????????????????????thread
    class Thread1 implements Runnable {
        public void run() {
            //?????????????????????????????????
            if(!functiontest) {
                //????????? ?????????websocket??????
                initwebSocket();
                //???????????????
                mHandler.postDelayed(heartBeatRunnable, HEART_BEAT_RATE);
                //????????????lockflag??????
                mHandler.postDelayed(LockFlagRunnable,2*1000);

            }

        }
    }
    //Call api ??????????????????
    static class Thread2 implements  Runnable{
        @Override
        public void run() {
            HttpURLConnection Alarmconnection;
            URL SendUrl = null;
            try {
                SendUrl = new URL("http://imoeedge20220914134800.azurewebsites.net/api/UserTime");
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
            Calendar Gettime;
            String SendIP = PIP;
            try {

                Alarmconnection = (HttpURLConnection) SendUrl.openConnection();
                Alarmconnection.setRequestMethod("POST");
                Alarmconnection.setDoInput(true);
                Alarmconnection.setDoOutput(true);
                Alarmconnection.setUseCaches(false);
                Alarmconnection.setRequestProperty("Content-Type", "application/json");
                Alarmconnection.connect();
                Gettime = Calendar.getInstance();
                DateTimeFormatter dtf = null;

                dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");


                if(!SendIP.isEmpty())
                {
                    SendIP= SendIP.replaceAll("\"","").replaceFirst("ip:","").replace("{","").replace("}","");
                    Log.e("IP",SendIP);
                }
                else
                {
                    return;
                }
                Data data = null;
                data = new Data(SendIP, dtf.format(LocalDateTime.now()), ID,owner);

                JSONObject jsonObject = new JSONObject();
                try{

                    jsonObject.put("publicip",data.publicip);
                    jsonObject.put("uploadtime",data.uploadtime);
                    jsonObject.put("serialnumber",data.serialnumber);
                    jsonObject.put("owner",data.owner);
                    Log.e("HTTP",jsonObject.toString());


                }
                catch(JSONException e) {
                    e.printStackTrace();

                }

                OutputStream outputStream = Alarmconnection.getOutputStream();
                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(outputStream, StandardCharsets.UTF_8));
                writer.write(jsonObject.toString());
                writer.flush();
                writer.close();
                int responseCode = Alarmconnection.getResponseCode();
                Log.e("API","send");
                if(responseCode == HttpURLConnection.HTTP_OK)
                {
                    Log.e("HTTP","Ok");

                }
                else
                {
                    Log.e("HTTP","not");
                }
                Alarmconnection.disconnect();

            }
            catch (Exception e)
            {
                e.printStackTrace();
                Log.e("APIFAIL","API :" + SendUrl +" conn fail");
            }
        }
    }
    //websocket ???????????????
    private  void closeReceiveConnect()
    {
        try{
            if(null != websocket)
            {
                websocket.close(1000);
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        finally {
            websocket = null;
        }

    }



    //???????????????
    private  void reconnectWs(){
        mHandler.removeCallbacks(heartBeatRunnable);
        new Thread()
        {
            @Override
            public void run()
            {
                try
                {
                    Log.e("reconn","reconn");
                    websocket.reconnectBlocking();
                }
                catch (InterruptedException e)
                {
                    e.printStackTrace();
                }
            }
        }.start();
    }
    private static final Handler handler=new Handler();

    //????????????server?????????
    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {
        // The service is starting, due to a call to startService()
        Log.e("TestService","restart");
        httpCall(ipurl.toString());
        if(!working)
        {
           // closeReceiveConnect();
            try {
                initViews();
            } catch (IOException exception) {
                exception.printStackTrace();
            }
            working = true;
            //compName = new ComponentName(ServerService.this,MyAdmin.class);
            boolean isActive = devicePolicyManager.isAdminActive(compName);

            if(isActive)
            {
                Thread1.start();

                Log.e("TestService","restart thread");
            }
            else
            {
                Intent intent1= new Intent();
                intent1.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent1.setAction(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN);
                intent1.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN,compName);
                startActivity(intent1);
            }

        }
        return Service.START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent)
    {
        // A client is binding to the service with bindService()
        Log.v("TestService","bindService");
        return mBinder;
    }

    @Override
    public boolean onUnbind(Intent intent)
    {
        // All clients have unbound with unbindService()
        return mAllowRebind;
    }

    @Override
    public void onRebind(Intent intent)
    {
        // A client is binding to the service with bindService(), after onUnbind() has already been called
    }


    //Service ??????????????????????????????????????????????????????
    @Override
    public void onDestroy()
    {
        super.onDestroy();
        // The service is no longer used and is being destroyed
        if(Block_Flag)//?????????????????????????????????,true ????????????
        {
            windowManager.removeView(floatView);
            lockManager.removeView(mMoniterView);
            unregisterReceiver(mHomeKeyBroadcastReceiver);
            Block_Flag = false;
        }
        //??????websocket
        closeReceiveConnect();
        Log.e("TestService","I am died");
        unregisterReceiver(mAlarmBroadcastReceiver);
        //??????Service
        //API > 26
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(new Intent(this, ServerService.class));
        }
    }


}
