package com.group12.wifip2pservicetest;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.IntentFilter;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.nsd.WifiP2pDnsSdServiceInfo;
import android.net.wifi.p2p.nsd.WifiP2pDnsSdServiceRequest;
import android.os.Build;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, WifiP2pManager.PeerListListener {

    private static final int SERVICE_BROADCASTING_INTERVAL = 1000;
    private static final int SERVICE_DISCOVERING_INTERVAL = 1000;
    WifiP2pManager manager;
    WifiP2pManager.Channel channel;
    BroadcastReceiver receiver;
    WifiP2pDnsSdServiceInfo serviceInfo;
    WifiP2pDnsSdServiceRequest serviceRequest;
    IntentFilter intentFilter;

    private String TAG = "===================";
    RecyclerView recyclerView;
    Button discoverServiceButton, broadcastServiceButton;

    MyAdapter myAdapter;

    ArrayList<Map<String, String>> devices;

    Handler mServiceBroadcastingHandler, mServiceDiscoveringHandler;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        manager = (WifiP2pManager) getSystemService(Context.WIFI_P2P_SERVICE);
        channel = manager.initialize(this, getMainLooper(), null);

        receiver = new WifiDirectBroadcastReceiver(manager, channel, this);

        intentFilter = new IntentFilter();
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);


        recyclerView = findViewById(R.id.recyclerView);
        discoverServiceButton = findViewById(R.id.discoverServiceButton);
        broadcastServiceButton = findViewById(R.id.broadcastServiceButton);

        discoverServiceButton.setOnClickListener(this);
        broadcastServiceButton.setOnClickListener(this);

        mServiceBroadcastingHandler = new Handler();
        mServiceDiscoveringHandler = new Handler();

        devices = new ArrayList<>();



    }

    @Override
    public void onClick(View v) {

        if (v.getId() == R.id.broadcastServiceButton)
        {
            Toast.makeText(this, "Broadcast service button pressed", Toast.LENGTH_SHORT).show();
            startBroadcastingService();
        }
        else if(v.getId() == R.id.discoverServiceButton)
        {
            Toast.makeText(this, "Discover service button pressed", Toast.LENGTH_SHORT).show();
            prepareServiceDiscovery();
            startServiceDiscovery();

        }
    }

    private Runnable mServiceBroadcastingRunnable = new Runnable() {
        @Override
        public void run() {
            manager.discoverPeers(channel, new WifiP2pManager.ActionListener() {
                @Override
                public void onSuccess() {
                }

                @Override
                public void onFailure(int error) {
                }
            });
            mServiceBroadcastingHandler.postDelayed(mServiceBroadcastingRunnable, SERVICE_BROADCASTING_INTERVAL);
        }
    };

    public void startBroadcastingService(){

        manager.clearLocalServices(channel, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                Toast.makeText(MainActivity.this, "local services cleared", Toast.LENGTH_SHORT).show();

                Map<String, String> record = new HashMap<>();
                record.put("From", "Dublin");
                record.put("To", "Cork");

                serviceInfo = WifiP2pDnsSdServiceInfo.newInstance("_test", "_presence._tcp", record);

                manager.addLocalService(channel, serviceInfo, new WifiP2pManager.ActionListener()
                {
                    @Override
                    public void onSuccess() {
                        // service broadcasting started
                        Toast.makeText(MainActivity.this, "SERVICE BROADCAST SUCCESSFUL", Toast.LENGTH_SHORT).show();
                        mServiceBroadcastingHandler.postDelayed(mServiceBroadcastingRunnable, SERVICE_BROADCASTING_INTERVAL);
                    }

                    @Override
                    public void onFailure(int error) {
                        // react to failure of adding the local service
                        if (error == WifiP2pManager.ERROR)
                            Toast.makeText(MainActivity.this, "ERROR", Toast.LENGTH_SHORT).show();
                        else if (error == WifiP2pManager.P2P_UNSUPPORTED)
                            Toast.makeText(MainActivity.this, "P2P Unsupported", Toast.LENGTH_SHORT).show();
                        else if (error == WifiP2pManager.BUSY)
                        Toast.makeText(MainActivity.this, "BUSY", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onFailure(int error) {
                Toast.makeText(MainActivity.this, "unable to clear local service", Toast.LENGTH_SHORT).show();
                // react to failure of clearing the local services
            }
        });
    }

    private Runnable mServiceDiscoveringRunnable = new Runnable() {
        @Override
        public void run() {
            prepareServiceDiscovery();
            startServiceDiscovery();
        }
    };

    public void prepareServiceDiscovery() {

        manager.setDnsSdResponseListeners(channel, new WifiP2pManager.DnsSdServiceResponseListener() {

            @Override
            public void onDnsSdServiceAvailable(String instanceName, String registrationType, WifiP2pDevice srcDevice) {
                Toast.makeText(MainActivity.this, "Service Detected", Toast.LENGTH_SHORT).show();
                myAdapter = new MyAdapter(devices, MainActivity.this);
                recyclerView.setLayoutManager(new LinearLayoutManager(MainActivity.this));
                recyclerView.setAdapter(myAdapter);
                recyclerView.setItemAnimator(new DefaultItemAnimator());
            }
            // do all the things you need to do with detected service

        }, new WifiP2pManager.DnsSdTxtRecordListener() {

            @Override
            public void onDnsSdTxtRecordAvailable(String fullDomainName, Map<String, String> record, WifiP2pDevice device) {
                // do all the things you need to do with detailed information about detected service

                HashMap<String, String> map = new HashMap<>(record);
                map.put("Name", device.deviceName);
                if(!devices.contains(map))
                    devices.add(map);


                Toast.makeText(MainActivity.this, "Detailed service info detected", Toast.LENGTH_SHORT).show();
            }
        });

        serviceRequest = WifiP2pDnsSdServiceRequest.newInstance();
    }

    public void startServiceDiscovery() {
        manager.removeServiceRequest(channel, serviceRequest, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
//                Toast.makeText(MainActivity.this, "Service request removed", Toast.LENGTH_SHORT).show();
                manager.addServiceRequest(channel, serviceRequest, new WifiP2pManager.ActionListener() {
                    @Override
                    public void onSuccess() {
//                        Toast.makeText(MainActivity.this, "service request added", Toast.LENGTH_SHORT).show();
                        manager.discoverServices(channel, new WifiP2pManager.ActionListener() {

                            @Override
                            public void onSuccess() {
                                //service discovery started
//                                Toast.makeText(MainActivity.this, "service discovery started", Toast.LENGTH_SHORT).show();
                                mServiceDiscoveringHandler.postDelayed(mServiceDiscoveringRunnable, SERVICE_DISCOVERING_INTERVAL);
                            }

                            @Override
                            public void onFailure(int error) {
                                Toast.makeText(MainActivity.this, "service discovery failed", Toast.LENGTH_SHORT).show();
                                // react to failure of starting service discovery
                            }
                        });
                    }
                    @Override
                    public void onFailure(int error) {
                        Toast.makeText(MainActivity.this, "failed to add service request", Toast.LENGTH_SHORT).show();
                        // react to failure of adding service request
                    }
                });
            }
            @Override
            public void onFailure(int reason) {
                Toast.makeText(MainActivity.this, "unable to remove service request", Toast.LENGTH_SHORT).show();
                // react to failure of removing service request
            }
        });
    }

    @Override
    public void onPeersAvailable(WifiP2pDeviceList peers) {
//        Toast.makeText(this, "On peers available", Toast.LENGTH_SHORT).show();
//        Toast.makeText(this, "List size: " + peers.getDeviceList().size(), Toast.LENGTH_SHORT).show();
    }

    /* register the broadcast receiver with the intent values to be matched */
    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(receiver, intentFilter);
    }
    /* unregister the broadcast receiver */
    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(receiver);
    }
}
