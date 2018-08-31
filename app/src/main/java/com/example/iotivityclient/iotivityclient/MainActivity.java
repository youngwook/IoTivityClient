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

        /** PlatformConfig class
         * @param context          app context
         * @param serviceType      indicate IN_PROC or OUT_OF_PROC
         * @param modeType         indicate whether we want to do server, client or both
         * @param ipAddress        ip address of server
         *                         if you specify 0.0.0.0 : it listens on any interface
         * @param port             port of server
         *                         if you specify 0 : next available random port is used
         *                         if you specify 5683 : client discovery can work even if
         *                         they don't specify port
         * @param qualityOfService quality of service
         */
        Log.d(TAG, "initialize IoTivity Client");
        //configuration of the Iotivity platform using PlatformConfig class
        PlatformConfig platformConfig = new PlatformConfig(this, ServiceType.IN_PROC, ModeType.CLIENT,
                "0.0.0.0", 0, QualityOfService.LOW);
        OcPlatform.Configure(platformConfig);

        /** OcPlatform.findResource(...)
         * API for Service and Resource Discovery.
         * <p>
         * Note: This API is for client side only.
         * </p>
         *
         * @param host                    Host IP Address of a service to direct resource discovery query.
         *                                If empty, performs multicast resource discovery query
         * @param resourceUri             name of the resource. If null or empty, performs search for all
         *                                resource names
         * @param connectivityTypeSet     Set of types of connectivity. Example: IP
         * @param onResourceFoundListener Handles events, success states and failure states.
         * @param qualityOfService        the quality of communication
         * @throws OcException if failure
         */
        Log.d(TAG, "find resource use transport ip");
        String uri = "coap://"+ IP +OcPlatform.WELL_KNOWN_QUERY + "?rt=core.temperature";
        try{
            //discover resources. when discovered callback onResourceFoundListener which we implemented
            OcPlatform.findResource("", uri, EnumSet.of(OcConnectivityType.CT_DEFAULT), this);
        } catch (OcException e){
            Log.d(TAG,"error for resource find :" + e.toString());
        }
    }

    @Override
    public synchronized void onResourceFound(OcResource ocResource) {
        //OcPlatform.OnResourceFoundListener as a callback when resources have been discovered & return OcResource
        if (null == ocResource){
            Log.d(TAG, "Found resource is invalid");
            return;
        }
        String resourceName = "/t/temperature";
        if(ocResource.getUri().equals(resourceName)){
            HashMap<String,String> queryResults = new HashMap<>();
            try{
                //return map of values to query & call back onGetListener
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
        //OcResource.OnGetListener as a callback & ocRepresentation hold map of values
        Log.d(TAG, "get temperature");
        try{
            //query needed value
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
