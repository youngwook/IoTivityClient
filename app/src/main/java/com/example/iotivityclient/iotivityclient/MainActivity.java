package com.example.iotivityclient.iotivityclient;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import org.iotivity.base.ModeType;
import org.iotivity.base.OcConnectivityType;
import org.iotivity.base.OcException;
import org.iotivity.base.OcHeaderOption;
import org.iotivity.base.OcPlatform;
import org.iotivity.base.OcRepresentation;
import org.iotivity.base.OcResource;
import org.iotivity.base.PlatformConfig;
import org.iotivity.base.QualityOfService;
import org.iotivity.base.ServiceType;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;

public class MainActivity extends AppCompatActivity implements OcPlatform.OnResourceFoundListener, OcResource.OnGetListener{

    private String TAG = "Android MainActivity for IoTivity Client";
    private TextView textView;

    private String IP = "192.168.0.10";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        textView = findViewById(R.id.textView);
    }
    protected void onDestroy() {
        super.onDestroy();
    }

    public void onClickGetTemperature(View view){
        Log.d(TAG, "ready to start IoTivity Client");
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Log.d(TAG, "start IoTivity Client");
                startIoTivityClient();
            }
        });
        Log.d(TAG, "started IoTivity Client");
    }
    public void msg(final String text){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                textView.setText(text);
            }
        });
    }
    public void startIoTivityClient(){
        Log.d(TAG, "initialize IoTivity Client");
        PlatformConfig platformConfig = new PlatformConfig(this, ServiceType.IN_PROC, ModeType.CLIENT,
                "0.0.0.0", 0, QualityOfService.LOW);
        OcPlatform.Configure(platformConfig);

        Log.d(TAG, "find resource use transport ip");
        String uri = "coap://"+ IP +OcPlatform.WELL_KNOWN_QUERY + "?rt=core.temperature";
        try{
            OcPlatform.findResource("", uri, EnumSet.of(OcConnectivityType.CT_DEFAULT), this);
        } catch (OcException e){
            Log.d(TAG,"error for resource find :" + e.toString());
        }
    }

    @Override
    public synchronized void onResourceFound(OcResource ocResource) {
        if (null == ocResource){
            Log.d(TAG, "Found resource is invalid");
            return;
        }
        String resourceName = "/t/temperature";
        if(ocResource.getUri().equals(resourceName)){
            HashMap<String,String> queryResults = new HashMap<>();
            try{
                ocResource.get(queryResults, this);
            }catch (OcException e){
                Log.d(TAG, "get resource data error :" + e.toString());
            }
        } else {
            Log.d(TAG, "no resource :" + resourceName);
        }

    }

    @Override
    public synchronized void onFindResourceFailed(Throwable throwable, String s) {
        Log.d(TAG, "find resource request has failed" + throwable.toString());
    }

    @Override
    public synchronized void onGetCompleted(List<OcHeaderOption> list, OcRepresentation ocRepresentation) {
        Log.d(TAG, "get temperature");
        try{
            String temp = ocRepresentation.getValue("temperature");
            msg(temp);
            Log.d(TAG, "temperature is: " + temp);
        }catch (OcException e) {
            Log.d(TAG,"error for get temperature :" + e.toString());
        }
    }

    @Override
    public synchronized void onGetFailed(Throwable throwable) {
        Log.d(TAG, "GET request has failed" + throwable.toString());
    }
}
