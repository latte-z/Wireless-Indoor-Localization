package club.lattez.wirelessindoorlocalization.Adapter;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import club.lattez.wirelessindoorlocalization.Model.WifiInfo;
import club.lattez.wirelessindoorlocalization.R;

public class WifiInfoAdapter extends RecyclerView.Adapter<WifiInfoAdapter.ViewHolder>{
    private List<WifiInfo> mWifiInfoList;

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView wifiBssid;
        TextView wifiSsid;
        TextView wifiRssi;

        public ViewHolder(View view) {
            super(view);
            wifiBssid = view.findViewById(R.id.wifi_bssid);
            wifiSsid = view.findViewById(R.id.wifi_ssid);
            wifiRssi = view.findViewById(R.id.wifi_rssi);
        }
    }

    public WifiInfoAdapter(List<WifiInfo> wifiInfoList) {
        mWifiInfoList = wifiInfoList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.wifi_info, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        WifiInfo wifiInfo = mWifiInfoList.get(position);
        holder.wifiBssid.setText(wifiInfo.getBssid());
        holder.wifiSsid.setText(wifiInfo.getSsid());
        holder.wifiRssi.setText("" + wifiInfo.getRssi());
    }

    @Override
    public int getItemCount() {
        return mWifiInfoList.size();
    }
}
