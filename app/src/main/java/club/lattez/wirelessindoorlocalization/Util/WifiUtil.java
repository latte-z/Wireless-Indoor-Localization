package club.lattez.wirelessindoorlocalization.Util;

import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;

import java.util.ArrayList;
import java.util.List;

import club.lattez.wirelessindoorlocalization.Model.WifiInfo;

public class WifiUtil {

    public static void startWifiScan(WifiManager wifiManager) {
        if (null != wifiManager) {
            wifiManager.startScan();
        }
    }

    public static List<WifiInfo> getWifiScanResult(WifiManager wifiManager) {
        if (null != wifiManager) {
            List<ScanResult> scanResultList = wifiManager.getScanResults();
            List<WifiInfo> returnList = new ArrayList<>();
            for (ScanResult scanResult : scanResultList) {
                // 存放每个 AP 的 MAC/SSID/RSSI
                WifiInfo wifiInfo = new WifiInfo(scanResult.BSSID, scanResult.SSID, scanResult.level);
                returnList.add(wifiInfo);
            }
            return returnList;
        } else {
            return null;
        }
    }
}
