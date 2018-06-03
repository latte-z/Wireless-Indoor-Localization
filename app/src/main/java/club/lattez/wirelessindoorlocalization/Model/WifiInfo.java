package club.lattez.wirelessindoorlocalization.Model;

public class WifiInfo {
    private String bssid;
    private String ssid;
    private int rssi;

    public WifiInfo(String bssid, String ssid, int rssi) {
        this.bssid = bssid;
        this.ssid = ssid;
        this.rssi = rssi;
    }



    public String getBssid() {
        return bssid;
    }

    public void setBssid(String bssid) {
        this.bssid = bssid;
    }

    public String getSsid() {
        return ssid;
    }

    public void setSsid(String ssid) {
        this.ssid = ssid;
    }

    public int getRssi() {
        return rssi;
    }

    public void setRssi(int rssi) {
        this.rssi = rssi;
    }
}
