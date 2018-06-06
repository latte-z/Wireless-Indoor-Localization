package club.lattez.wirelessindoorlocalization.Util;

import com.google.gson.Gson;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;

public class HttpUtil {

    private static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");

    public static void sendOkHttpRequest(String address, okhttp3.Callback callback) {
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder().url(address).build();
        client.newCall(request).enqueue(callback);
    }

    public static void putJson(String address, Object RequestObject, okhttp3.Callback callback) {
        Gson gson = new Gson();
        String jsonStr = gson.toJson(RequestObject);
        OkHttpClient client = new OkHttpClient();
        RequestBody body = RequestBody.create(JSON, jsonStr);
        Request request = new Request.Builder().url(address).put(body).build();
        client.newCall(request).enqueue(callback);
    }
}
