package net.onlyid.onlyid_demo;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import net.onlyid.onlyid_sdk.OnlyID;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.FormBody;
import okhttp3.Request;
import okhttp3.RequestBody;

public class MainActivity extends Activity implements OnlyID.AuthListener {
    static final String CLIENT_ID = "5adac916904be93f3f621003", CLIENT_SECRET = "f71c0c12997a00cf95fe82e130a45711";
    // 你的服务端地址
    static final String MY_URL = "http://demo.onlyid.net:3002/";
    private static final String TAG = "OnlyID_Demo";
    TextView textView, textView2, textView3;
    boolean clientTokenFlow = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        textView = findViewById(R.id.textView);
        textView2 = findViewById(R.id.textView2);
        textView3 = findViewById(R.id.textView3);

        MyHttp.initOkHttpClient(this);
    }

    /**
     * 演示客户端token流程 适合大多数开发者
     * @param view
     */
    public void auth(View view) {
        // 最后两字段为自定义选项 请参看文档查看用法 或直接传null
        OnlyID.auth(this, CLIENT_ID, null, this, CLIENT_SECRET, null, null);
    }

    /**
     * 演示服务端中转流程 适合安全性要求高的开发者
     * @param view
     */
    public void auth1(View view) {
        clientTokenFlow = false;
        OnlyID.auth(this, CLIENT_ID, null, this, null, null, null);
    }

    @Override
    public void onAuthResponse(OnlyID.AuthResponse authResponse) {
        Log.d(TAG, "onAuthResponse: " + authResponse);
        textView2.setText("SDK返回：" + authResponse.toString());
        if (authResponse.code == OnlyID.ErrCode.CANCEL) {
            // do nothing
            return;
        }
        if (authResponse.code == OnlyID.ErrCode.NETWORK_ERR) {
            Toast.makeText(this, "网络错误, 请重试", Toast.LENGTH_LONG).show();
            return;
        }

        // 使用客户端token流程时 sdk直接返回accessToken
        if (clientTokenFlow) {
            RequestBody requestBody = new FormBody.Builder().add("accessToken", authResponse.accessToken).build();
            Request request = new Request.Builder().url(MY_URL + "auth-app").post(requestBody).build();
            MyHttp.request(request, new MyHttp.MyCallback() {
                @Override
                public void onFailure(Call call, IOException e, Integer code) {
                    textView3.setText("请求错误：" + e.toString());
                }

                @Override
                public void onSuccess(Call call, String s) {
                    textView3.setText("服务端返回：" + s);
                }
            });
        }
        // 使用服务端中转流程时 sdk返回authCode
        else {
            RequestBody requestBody = new FormBody.Builder().add("code", authResponse.authCode).build();
            Request request = new Request.Builder().url(MY_URL + "auth-app1").post(requestBody).build();
            MyHttp.request(request, new MyHttp.MyCallback() {
                @Override
                public void onFailure(Call call, IOException e, Integer code) {
                    textView3.setText("请求错误：" + e.toString());
                }

                @Override
                public void onSuccess(Call call, String s) {
                    textView3.setText("服务端返回：" + s);
                }
            });
        }
    }
}
