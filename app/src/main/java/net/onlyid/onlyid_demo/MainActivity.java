package net.onlyid.onlyid_demo;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import net.onlyid.onlyid_sdk.AuthActivity;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class MainActivity extends Activity {
    TextView tip, tip1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        tip = findViewById(R.id.tip);
        tip1 = findViewById(R.id.tip1);
    }

    public void login(View view) {
        Intent intent = new Intent(this, AuthActivity.class);
        intent.putExtra("clientId", "5adac916904be93f3f621003");
        startActivityForResult(intent, 1);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode != 1) return;

        if (resultCode == RESULT_CANCELED) { tip.setText("用户取消"); return; }

        if (resultCode == AuthActivity.RESULT_NETWORK_ERR) { tip.setText("网络错误"); return; }

        String code = data.getStringExtra("code");

        // 生产环境使用时，获取用户信息建议在服务端进行，以防泄露你的client secret
        RequestBody requestBody = new FormBody.Builder().add("code", code)
                .add("client_id", "5adac916904be93f3f621003")
                .add("client_secret", "f71c0c12997a00cf95fe82e130a45711").build();
        Request request = new Request.Builder().url("https://my.onlyid.net/user").post(requestBody).build();
        new OkHttpClient().newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, final IOException e) {
                runOnUiThread(() -> { tip.setText("网络错误：\n" + e); });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                try (ResponseBody responseBody = response.body()) {
                    final String res = responseBody.string();
                    if (!response.isSuccessful()) {
                        runOnUiThread(() -> { tip.setText("登录失败，错误信息：\n" + res); });
                        return;
                    }
                    runOnUiThread(() -> { tip.setText("登录成功，用户信息：\n" + res); });
                }
            }
        });
        tip1.setText("再试一次：");
    }
}
