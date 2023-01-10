package com.example.bootservicefor7to9;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;

public class FirstActivity extends AppCompatActivity {

    TextView SNTEXT;
    Spinner URL_Spinner;
    ImageView IV;
    Button btnSend;
    String SN,domain;
    boolean Hrecord = false;
    boolean webtest = true;
    HttpURLConnection connection;
    URL url;
    JSONObject jsonObject = null;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_first);
       // getSupportActionBar().setBackgroundDrawable(new ColorDrawable(ContextCompat.getColor(FirstActivity.this, R.color.light_blue_400)));
        //getSupportActionBar().setDisplayShowTitleEnabled(false);
        Window window = FirstActivity.this.getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.setStatusBarColor(ContextCompat.getColor(FirstActivity.this, R.color.purple_500));

        SNTEXT = findViewById(R.id.SNTEXT);
        //SN碼在7-9可以直接拿到，所以只要有分區資訊Spinner就好了
        //Spinner text
        URL_Spinner = findViewById(R.id.URL_Spinner);
        //Spinner value(URL)
        String[] link = getResources().getStringArray(R.array.URL_list_value);
        //取出資料放入spinner
        ArrayAdapter<CharSequence> adapter =
                ArrayAdapter.createFromResource(this,    //對應的Context
                        R.array.URL_list,                             //資料選項內容
                       R.layout.myspinner);  //預設Spinner未展開時的View(預設及選取後樣式)

        adapter.setDropDownViewResource(R.layout.myspinner_background);
        URL_Spinner.setAdapter(adapter);
        btnSend = findViewById(R.id.btnSend);



        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.O)
        {
            SN = Build.SERIAL;
        }
        else{
            SN = Build.getSerial();
        }
        Log.i("First information",SN);
        SNTEXT.setText("此裝置SN碼為:" + SN);
        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //根據選擇提取對應的URL
                domain = link[URL_Spinner.getSelectedItemPosition()];
                Log.d("HTTP",domain);
                StringBuilder response = new StringBuilder();

                try {
                    if(webtest)
                    {
                        //server for testing function(debug)
                        //自製測試用web service
                        url = new URL("http://imoeedge20220914134800.azurewebsites.net/api/UnitInfro");

                    }
                    else
                    {
                        //real server we used
                        //抓分區資訊輸入當連接link
                        if(!domain.equals("0"))
                        {
                            url = new URL(domain);
                        }


                    }
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                }

                if (!SN.isEmpty()&&!domain.equals("0")) {
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                connection = (HttpURLConnection) url.openConnection();
                                connection.setRequestMethod("GET");
                                connection.setRequestProperty("SN", SN);
                                connection.connect();


                                int responseCode = connection.getResponseCode();
                                //存有本機SN
                                if(responseCode == HttpURLConnection.HTTP_OK )
                                {
                                    InputStream is = connection.getInputStream();
                                    BufferedReader reader = new BufferedReader(new InputStreamReader(is));
                                    String line;
                                    while ((line = reader.readLine()) != null) {

                                        response.append(line);
                                        response.append('\r');
                                    }
                                    jsonObject = new JSONObject(response.toString().trim());
                                    Log.e("HTTPJSONObject",jsonObject.toString());
                                    Log.e("HTTP","Ok");
                                    if(jsonObject.get("statusCode").toString().equals("00"))
                                    {
                                        Hrecord = true;
                                    }



                                }
                                else//不存在本機SN記錄
                                {


                                    Hrecord = false;
                                    Log.e("HTTP","Not Ok");
                                }
                                connection.disconnect();
                            }
                            catch(Exception e)
                            {
                                Hrecord = false;
                                e.printStackTrace();
                                Log.e("HTTP",e.toString());
                                Log.e("HTTP","conn fail");
                            }
                            if(Hrecord) {

                                Intent intent = new Intent(FirstActivity.this, MainActivity.class);
                                intent.putExtra("SN", SN);
                                try {
                                    intent.putExtra("socketaddress", jsonObject.getString("socketaddress"));
                                    intent.putExtra("apiaddress", jsonObject.getString("apiaddress"));
                                    intent.putExtra("unitinfro", jsonObject.getString("unitinfro"));
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }

                                Log.e("SN", SN);
                                setResult(RESULT_OK, intent);
                                finish();
                            }
                            else
                            {

                                // for testing,如果testing會回傳假資料
                                if(webtest)
                                {

                                    Intent intent = new Intent(FirstActivity.this, MainActivity.class);
                                    intent.putExtra("SN", SN);

                                        intent.putExtra("socketaddress", "ws://imoeedge20220914134800.azurewebsites.net/api/WebSoket?nickName=");
                                        intent.putExtra("apiaddress", "http://imoeedge20220914134800.azurewebsites.net/api/Usertime");
                                        intent.putExtra("unitinfro", "基隆市市立八堵國小");
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            Toast.makeText(getApplicationContext(),
                                                    "測試",Toast.LENGTH_LONG).show();
                                            Log.e("HTTP","fail text");
                                        }
                                    });

                                    Log.e("SN", SN);
                                    setResult(RESULT_OK, intent);

                                    finish();
                                }
                                if(!webtest)
                                {
                                    runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast.makeText(getApplicationContext(),
                                                "登記失敗，請檢查網路連線並確定選擇之所屬分區是否正確",Toast.LENGTH_LONG).show();
                                        Log.e("HTTP","fail text");
                                    }
                                    });
                                }



                            }
                        }
                    }

                    ).start();




                }
                else
                {
                    String errormessage = "缺失以下資訊:";
                    if(domain.equals("0"))
                    {
                        errormessage += "分區資訊\n";
                    }
                    Log.e("HTTP",errormessage);
                }

            }
        });
    }
}

