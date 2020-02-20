package com.example.day;

import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;

import static androidx.appcompat.widget.AppCompatDrawableManager.get;

public class MainActivity extends AppCompatActivity {
    private Button btGet;
    private Button btPost;
    private Button btDownload;
    private Button btDownloadDuan;
    private ProgressBar bar;
    private Button btUpload;

    private static final String TAG = "MainActivity";

    private String post_url = "https://www.wanandroid.com/user/register";
    private String get_url = "http://www.qubaobei.com/ios/cf/dish_list.php?stage_id=1&limit=20&page=1";
    private String download_url="http://uvideo.spriteapp.cn/video/2019/0512/56488d0a-7465-11e9-b91b-1866daeb0df1_wpd.mp4";
    private String douying_url = "http://api.budejie.com/api/api_open.php?a=list&c=data&type=41&page=1";

    private long start;
    private long end;

    private Handler handler = new Handler();
    /***
     *
     1。简介
     okhttp是一个第三方类库，用于android中请求网络。
     这是一个开源项目,是安卓端最火热的轻量级框架,由移动支付Square公司贡献(该公司还贡献了Picasso和LeakCanary) 。用于替代HttpUrlConnection和Apache HttpClient(android API23 里已移除HttpClient)。
     2。功能
     （1）get请求  string()
     （2）post请求
     （3）下载文件 byteStream()
     （4）断点续传
     （5）上传文件

     3。实现流程：
     （1）client 客户端对象
     （2）request请求对象
     （3）call 连接
     （4）response响应对象

        responseBody ---string()  byteStream()
     *
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(new String[]{
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.READ_EXTERNAL_STORAGE
            },101);
        }
        init();
    }

    private void init() {
        btGet = (Button) findViewById(R.id.bt_get);
        btPost = (Button) findViewById(R.id.bt_post);
        btDownload = (Button) findViewById(R.id.bt_download);
        btDownloadDuan = (Button) findViewById(R.id.bt_download_duan);
        bar = (ProgressBar) findViewById(R.id.bar);
        btUpload = (Button) findViewById(R.id.bt_upload);

    }

    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.bt_get: //get请求网络数据 json:
                get();
                break;
            case R.id.bt_post:
                post();
                break;
            case R.id.bt_download://下载文件 更新进度条
                download();
                break;
            case R.id.bt_download_duan:
                duan();
                break;
            case R.id.bt_upload:
                douY();
                break;
        }
    }

    private void douY() {
        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(60000, TimeUnit.MILLISECONDS)
                .readTimeout(60000, TimeUnit.MILLISECONDS)
                .writeTimeout(60000, TimeUnit.MILLISECONDS)
                .build();
        Request request = new Request.Builder()
                .get()
                .url(douying_url)
                .header("User-Agent","PostmanRuntime/7.21.0")//报403 禁止访问 添加头User-Agen
                .build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, final IOException e) {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(MainActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();

                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String string = response.body().string();
                Log.d(TAG, "onResponse: "+string);
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(MainActivity.this, ""+string, Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    private void duan2(){
        File file = new File("/sdcard/xinxin,mp4");
        if (file.exists()) {
            start=file.length();
        }else{
            start=0;
        }
        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(60000, TimeUnit.MILLISECONDS)
                .readTimeout(60000, TimeUnit.MILLISECONDS)
                .writeTimeout(60000, TimeUnit.MILLISECONDS)
                .build();
        final Request request = new Request.Builder()
                .get()
                .url(download_url)
                .header("Range","bytes="+start+"-"+end)
                .build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Toast.makeText(MainActivity.this, "失败", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                Log.d(TAG, "onResponse: 第二次"+response.body().contentLength());
                InputStream inputStream = response.body().byteStream();
                FileOutputStream fileOutputStream = new FileOutputStream("/sdcard/xinxin,mp4", true);
                byte[] bys = new byte[1024];
                int len= (int) start;
                int count= (int) start;
                while ((len = inputStream.read(bys)) != -1) {
                    try {
                        Thread.sleep(5);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    count+=len;
                    fileOutputStream.write(bys,0,len);
                    final int progress= (int) (count*100/end);
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            bar.setProgress(progress);
                        }
                    });
                }
            }
        });
    }

    private void duan() {
        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(60000, TimeUnit.MILLISECONDS)
                .readTimeout(60000, TimeUnit.MILLISECONDS)
                .writeTimeout(60000, TimeUnit.MILLISECONDS)
                .build();
        final Request request = new Request.Builder()
                .get()
                .url(download_url)
                .build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(MainActivity.this, "失败", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                end=response.body().contentLength();
                Log.d(TAG, "onResponse: 第一次"+end);
                duan2();
            }
        });
    }

    private void post() {
        //client对象
        OkHttpClient client = new OkHttpClient.Builder()
                .writeTimeout(60000, TimeUnit.MILLISECONDS)
                .readTimeout(60000, TimeUnit.MILLISECONDS)
                .connectTimeout(60000, TimeUnit.MILLISECONDS)
                .build();
        //请求体 username,password,repassword
        FormBody formBody = new FormBody.Builder()
                .add("username", "我去额")
                .add("password", "123456")
                .add("repassword", "123456")
                .build();
        //request对象
        Request request = new Request.Builder()
                .url(post_url)
                .post(formBody)
                .build();
        //连接
        Call call = client.newCall(request);
        //回调
        call.enqueue(new Callback() {
            //失败
            @Override
            public void onFailure(Call call, final IOException e) {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }
            //成功
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                ResponseBody body = response.body(); //获得请求体
                final String string = body.string(); //获得数据
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(MainActivity.this, string, Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    private void download() {
        //client对象
        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(60000, TimeUnit.MILLISECONDS)
                .readTimeout(60000, TimeUnit.MILLISECONDS)
                .writeTimeout(60000, TimeUnit.MILLISECONDS)
                .build();
        //request 对象
        Request request = new Request.Builder()
                .get()
                .url(download_url)
                .build();
        //连接
        Call call = client.newCall(request);
        //接口回调
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                //子线程 不能更新UI
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(MainActivity.this, "失败", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                ResponseBody body = response.body();//获得响应体
                long max=body.contentLength(); //获取视频的总大小
                InputStream inputStream = body.byteStream(); //获得输入流
                //SD卡写的输出流
                FileOutputStream fileOutputStream = new FileOutputStream("/sdcard/dengziqi.mp4");
                //边读编写
                byte[] bys = new byte[1024];
                int len = 0;
                int count = 0; //下载进度
                while ((len = inputStream.read(bys)) != -1) {
                    fileOutputStream.write(bys,0,len);
                    count+=len;
                    //获得0 - 100的进度
                    final int progress= (int) (count*100/max);
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            bar.setProgress(progress);
                        }
                    });
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(MainActivity.this, "下载完成", Toast.LENGTH_SHORT).show();
                        }
                    });
                }

            }
        });
    }

    private void get(){
        //TODO client对象
        OkHttpClient client = new OkHttpClient.Builder()
                .writeTimeout(60000, TimeUnit.MILLISECONDS)
                .readTimeout(60000, TimeUnit.MILLISECONDS)
                .connectTimeout(60000, TimeUnit.MILLISECONDS)
                .build();
        //TODO request对象
        final Request request = new Request.Builder()
                .get()
                .url(get_url)
//                .header()
                .build();
        //TODO call连接
        Call call = client.newCall(request);
        //TODO
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.d(TAG, "onFailure: "+e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                //成功 不能更新UI 在子线程中
                ResponseBody body = response.body();//响应体
                String json = body.string(); //响应体---> string字符串
                Log.d(TAG, "onResponse: "+json);
            }
        });

    }
}
