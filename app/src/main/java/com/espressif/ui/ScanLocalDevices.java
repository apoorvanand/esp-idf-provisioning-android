package com.espressif.ui;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

import com.espressif.provision.utils.UPnPDevice;
import com.espressif.provision.utils.UPnPDiscovery;
import com.espressif.provision.R;

import java.util.ArrayList;
import java.util.HashSet;

public class ScanLocalDevices extends AppCompatActivity {
    private ArrayList<UPnPDevice> SSDPdevices;
    private ArrayAdapter<String> SSDPadapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_devices);

        Toolbar toolbar = (Toolbar) findViewById(R.id.scantoolbar);
        toolbar.setTitle("Devices on Local Network");

        SSDPdevices = new ArrayList<>();
        ArrayList<String> devNames = new ArrayList<>();

        ListView listView = findViewById(R.id.connected_devices_list);

        SSDPadapter= new ArrayAdapter<>(this,
                android.R.layout.simple_list_item_1,
                android.R.id.text1,
                devNames);
        listView.setAdapter(SSDPadapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int pos, long l) {
//                progressBar.setVisibility(View.VISIBLE);
                Log.d("WiFiScanList","Device to be connected -"+SSDPdevices.get(pos));
                manageDevice(SSDPdevices.get(pos));
            }

        });


        Button scanDevices = findViewById(R.id.devices_scan);
        scanDevices.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("ScanLocalDevices","Initiating broadcast");
                searchDevices();
            }
        });
    }

    private void manageDevice (UPnPDevice name){

    }
    private void searchDevices(){
        String customQuery = "M-SEARCH * HTTP/1.1" + "\r\n" +
                "HOST: 239.255.255.250:1900" + "\r\n" +
                "MAN: \"ssdp:discover\"" + "\r\n" +
                "MX: 3"+ "\r\n" +
//                "ST: urn:schemas-upnp-org:service:AVTransport:1" + "\r\n" + // Use for Sonos
//                "ST: urn:schemas-upnp-org:device:InternetGatewayDevice:1" + "\r\n" + // Use for Routers
//                "ST: urn:schemas-upnp-org:device:MediaRenderer:1" + "\r\n" + // Use for Routers
                "ST: ssdp:all" + "\r\n" + // Use this for all UPnP Devices (DEFAULT)
                "\r\n";
        int customPort = 1900;
        String customAddress = "239.255.255.250";

        UPnPDiscovery.discoveryDevices(this, new UPnPDiscovery.OnDiscoveryListener() {
            @Override
            public void OnStart() {
                Log.d("UPnPDiscovery", "Starting discovery");
            }

            @Override
            public void OnFoundNewDevice(UPnPDevice device) {
                Log.d("UPnPDiscovery", "Found new device: " + device.toString());
                 final UPnPDevice foundDevice = device;
                 runOnUiThread(new Runnable() {
                                      public void run() {
                                          boolean deviceExists = false;
                                              for(UPnPDevice alreadyHere: SSDPdevices) {
                                                  if (foundDevice.getHostAddress().equals(alreadyHere.getHostAddress())) {
                                                      deviceExists = true;
                                                      Log.d("ManageDevice","Device already exists");
                                                      break;
                                                  }
                                              }
                                              if (!deviceExists) {

                                                  Log.d("ManageDevice", "Adding to list adapter");
                                                  SSDPdevices.add(foundDevice);
                                                  SSDPadapter.add(foundDevice.getFriendlyName()+" | " +foundDevice.getHostAddress()+ "  | " + foundDevice.getManufacturer());
                                                  SSDPadapter.notifyDataSetChanged();
                                              }
                                          }
//                                          SSDPdevices.add(foundDevice);
//                                          SSDPadapter.add(foundDevice.getFriendlyName());
//

                                  });
            }

            @Override
            public void OnFinish(HashSet<UPnPDevice> devices) {
                Log.d("UPnPDiscovery", "Finish discovery");
            }

            @Override
            public void OnError(Exception e) {
                Log.d("UPnPDiscovery", "Error: " + e.getLocalizedMessage());
            }
        }, customQuery, customAddress, customPort);
    }
}