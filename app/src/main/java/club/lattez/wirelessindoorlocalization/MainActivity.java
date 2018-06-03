package club.lattez.wirelessindoorlocalization;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import java.util.List;

import club.lattez.wirelessindoorlocalization.Adapter.WifiInfoAdapter;
import club.lattez.wirelessindoorlocalization.Model.WifiInfo;
import club.lattez.wirelessindoorlocalization.Util.WifiUtil;

public class MainActivity extends AppCompatActivity {

    private WifiManager wifiManager;
    private WifiReceiver wifiReceiver;
    private RecyclerView recyclerView;
    private WifiInfoAdapter wifiInfoAdapter;
    private List<WifiInfo> wifiInfoList;

    private static final int WIL_ACCESS_FINE_LOCATION = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    WIL_ACCESS_FINE_LOCATION);
        }

        // 初始化一个 WiFi manager
        wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);

        // 初始化 receiver
        wifiReceiver = new WifiReceiver();
        registerReceiver(wifiReceiver, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));

        // 初始化周围 WiFi 信息
        wifiInfoList = initWifiInfo(wifiManager);

        // 初始化 RecyclerView
        recyclerView = findViewById(R.id.wifi_info_recycler_view);
        wifiInfoAdapter = new WifiInfoAdapter(wifiInfoList);
        recyclerView.setAdapter(wifiInfoAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(0, 0, 0, "重新扫描");
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case WIL_ACCESS_FINE_LOCATION:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // 获取到权限
                } else {
                    // 未获取到权限
                    onDestroy();
                }
                break;
            default:
                break;
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case 0:
                wifiManager.startScan();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onPause() {
        unregisterReceiver(wifiReceiver);
        super.onPause();
    }

    @Override
    protected void onResume() {
        registerReceiver(wifiReceiver, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
        super.onResume();
    }

    private List<WifiInfo> initWifiInfo(WifiManager wifiManager) {
        WifiUtil.startWifiScan(wifiManager);
        Toast.makeText(this, "开始扫描AP", Toast.LENGTH_SHORT).show();
        return WifiUtil.getWifiScanResult(wifiManager);
    }

    class WifiReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            // 更新 recyclerView 内数据
            recyclerView.removeAllViews();
            wifiInfoList.clear();
            wifiInfoList.addAll(WifiUtil.getWifiScanResult(wifiManager));
            wifiInfoAdapter.notifyDataSetChanged();
        }
    }
}
