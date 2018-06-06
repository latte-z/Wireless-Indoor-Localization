package club.lattez.wirelessindoorlocalization;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import club.lattez.wirelessindoorlocalization.Adapter.WifiInfoAdapter;
import club.lattez.wirelessindoorlocalization.Model.WifiInfo;
import club.lattez.wirelessindoorlocalization.UI.LocationActivity;
import club.lattez.wirelessindoorlocalization.Util.HttpUtil;
import club.lattez.wirelessindoorlocalization.Util.WifiUtil;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {

    private WifiManager wifiManager;
    private WifiReceiver wifiReceiver;
    private RecyclerView recyclerView;
    private WifiInfoAdapter wifiInfoAdapter;
    private List<WifiInfo> wifiInfoList;
    private Set<String> filterFingerPrinter = new HashSet<>(Arrays.asList("", ""));

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

        // 初始化底栏
        BottomNavigationView navigation = findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

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
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.navigation_home:
                    return true;
                case R.id.navigation_dashboard:
                    Intent intent = new Intent(MainActivity.this, LocationActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                    startActivity(intent);
                    return true;
            }
            return false;
        }
    };

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_refresh:
                wifiManager.startScan();
                Toast.makeText(this, "重新扫描中...", Toast.LENGTH_SHORT).show();
                break;
            case R.id.action_upload: {
                List<WifiInfo> wifiInfoList = WifiUtil.getWifiScanResult(wifiManager);
                int x = new Random().nextInt(10) + 1;
                int y = new Random().nextInt(10) + 1;
                Map<String, Integer> wifiRssi = new HashMap<>();
                for (WifiInfo wifiInfo : wifiInfoList) {
                    if (filter(wifiInfo, wifiRssi))
                        wifiRssi.put(wifiInfo.getBssid(), Math.abs(wifiInfo.getRssi()));
                }
                uploadCurPosition(x, y, wifiRssi);
                break;
            }
        }
        return super.onOptionsItemSelected(item);
    }

    private boolean filter(WifiInfo wifiInfo, Map<String, Integer> wifiRssi) {
        return filterFingerPrinter.contains(wifiInfo.getBssid());
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
        Toast.makeText(this, "开始扫描AP...", Toast.LENGTH_SHORT).show();
        return WifiUtil.getWifiScanResult(wifiManager);
    }

    private void uploadCurPosition(int x, int y, Map<String, Integer> wifiRssi) {
        Map<String, Object> uploadMap = new HashMap<>();
        uploadMap.put("x", x);
        uploadMap.put("y", y);
        uploadMap.put("wifiRssi", wifiRssi);
        List<Map<String, Object>> uploadList = new ArrayList<>();
        uploadList.add(uploadMap);
        String address = "http://192.168.2.19:8181/point/save";
        HttpUtil.putJson(address, uploadList, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
                Looper.prepare();
                Toast.makeText(MainActivity.this, "上传失败", Toast.LENGTH_SHORT).show();
                Looper.loop();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                Looper.prepare();
                Toast.makeText(MainActivity.this, "上传成功 " + response.body().string(), Toast.LENGTH_SHORT).show();
                Looper.loop();
            }
        });
    }

    class WifiReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            // 更新 recyclerView 内数据
            recyclerView.removeAllViews();
            wifiInfoList.clear();
            wifiInfoList.addAll(WifiUtil.getWifiScanResult(wifiManager));
            wifiInfoAdapter.notifyDataSetChanged();
            Toast.makeText(context, "重新扫描中...", Toast.LENGTH_SHORT).show();
        }
    }
}
