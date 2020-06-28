package net.onlyid.demo;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import org.json.JSONObject;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import net.onlyid.sdk.OnlyID;

public class MainActivity extends Activity {
    static final String TAG = "OnlyID";
    static final String CLIENT_ID = "73c6cce568d34a25ac426a26a1ca0c1e";
    static final String CLIENT_SECRET = "36c820ba83bb4944a0744208066e8bbf";

    TextView result;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        result = findViewById(R.id.result);
    }

    public void login(View view) {
        OnlyID.OAuthConfig config = new OnlyID.OAuthConfig(CLIENT_ID);
        OnlyID.oauth(this, config, new OnlyID.OAuthListener() {
            @Override
            public void onComplete(String code, String state) {
                Log.d(TAG, "onComplete: code= " + code + ", state= " + state);

                new Thread(() -> {
                    getUserInfo(code);
                }).start();
            }

            @Override
            public void onError(OnlyID.ErrCode errCode) {
                result.setText("登录失败，错误信息：\n" + errCode + "（" + errCode.msg + "）");
            }

            @Override
            public void onCancel() {
                result.setText("用户取消");
            }
        });
    }

    /**
     * 生产环境使用时，获取用户信息建议在服务端进行，以防泄露你的client secret
     */
    void getUserInfo(String code) {
        Response response = null;
        try {
            OkHttpClient httpClient = new OkHttpClient();

            JSONObject object = new JSONObject();
            object.put("clientSecret", CLIENT_SECRET);
            object.put("authorizationCode", code);
            Request request = new Request.Builder()
                    .url("https://www.onlyid.net/api/oauth/access-token")
                    .post(RequestBody.create(MediaType.get("application/json; charset=utf-8"), object.toString()))
                    .build();
            response = httpClient.newCall(request).execute();
            String respBody = response.body().string();
            if (!response.isSuccessful()) throw new Exception(respBody);

            response.close();
            Request request1 = new Request.Builder()
                    .url("https://www.onlyid.net/api/open/user-info")
                    .header("Authorization", new JSONObject(respBody).getString("accessToken"))
                    .build();
            response = httpClient.newCall(request1).execute();
            String respBody1 = response.body().string();
            if (!response.isSuccessful()) throw new Exception(respBody1);

            response.close();
            String formattedUserInfo = new JSONObject(respBody1).toString(2);
            runOnUiThread(() -> {
                result.setText("登录成功，用户信息：\n" + formattedUserInfo);
            });
        } catch (Exception e) {
            e.printStackTrace();
            if (response != null) response.close();

            runOnUiThread(() -> {
                result.setText("登录失败，错误信息：\n" + e.getMessage());
            });
        }
    }
}
