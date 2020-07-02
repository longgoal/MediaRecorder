package com.jsqix.camerarecord;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import java.util.List;

public class ChooseActivity extends AppCompatActivity implements View.OnClickListener {

    private Button mBtnActivity;
    private Button mBtnStartService;
    private Button mBtnStopService;
    private boolean mServiceRunning;
    private Intent mServiceIntent;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.chooseactivity_layout);

        mBtnActivity = (Button) findViewById(R.id.btn_activity);
        mBtnActivity.setOnClickListener(this);

        mBtnStartService = (Button) findViewById(R.id.btn_start_service);
        mBtnStartService.setOnClickListener(this);

        mBtnStopService = (Button) findViewById(R.id.btn_stop_service);
        mBtnStopService.setOnClickListener(this);


        mServiceIntent = new Intent(this,BackgroundVideoRecorder.class);

    }
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_activity:
                Intent intent = new Intent();
                intent.setClass(this,MainActivity.class);
                startActivity(intent);
                break;
            case R.id.btn_start_service:
                mBtnActivity.setEnabled(false);

                mBtnStartService.setEnabled(false);
                mBtnStopService.setEnabled(true);

                startService(mServiceIntent);
                mServiceRunning = true;
                break;
            case R.id.btn_stop_service:
                mBtnActivity.setEnabled(true);

                mBtnStartService.setEnabled(true);
                mBtnStopService.setEnabled(false);

                stopService(mServiceIntent);
                mServiceRunning = false;
                break;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        mServiceRunning = isServiceRunning(this,"com.jsqix.camerarecord.BackgroundVideoRecorder");
        if(mServiceRunning) {
            mBtnActivity.setEnabled(false);

            mBtnStartService.setEnabled(false);
            mBtnStopService.setEnabled(true);
        } else {
            mBtnActivity.setEnabled(true);

            mBtnStartService.setEnabled(true);
            mBtnStopService.setEnabled(false);
        }
    }
    public static boolean isServiceRunning(Context context,String serviceName){
        // 校验服务是否还存在
        ActivityManager am = (ActivityManager) context
                .getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningServiceInfo> services = am.getRunningServices(Integer.MAX_VALUE);
        for (ActivityManager.RunningServiceInfo info : services) {
            // 得到所有正在运行的服务的名称
            String name = info.service.getClassName();
            if (serviceName.equals(name)) {
                return true;
            }
        }
        return false;
    }


}
