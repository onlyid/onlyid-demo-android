package net.onlyid.onlyid_demo;

import android.content.Context;
import android.os.Handler;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

/**
 * Created by l on 2017/10/8.
 * 封装请求网络
 */

public class MyHttp {
    static OkHttpClient httpClient;
    static Handler handler = new Handler();

    static void initOkHttpClient(Context context) {
        httpClient = new OkHttpClient.Builder().build();
    }

    // 在ui线程回调
    static void request(final Request request, final MyCallback myCallback) {
        httpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(final Call call, final IOException e) {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        myCallback.onFailure(call, e, null);
                    }
                });
            }

            @Override
            public void onResponse(final Call call, final Response response) throws IOException {
                try (ResponseBody responseBody = response.body()) {
                    if (!response.isSuccessful()) {
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                myCallback.onFailure(call, new IOException("Unexpected code " + response), response.code());
                            }
                        });
                        return;
                    }

                    final String s = responseBody.string();
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            myCallback.onSuccess(call, s);
                        }
                    });
                }
            }
        });
    }

    interface MyCallback {
        void onFailure(Call call, IOException e, Integer code);
        void onSuccess(Call call, String s);
    }

}
