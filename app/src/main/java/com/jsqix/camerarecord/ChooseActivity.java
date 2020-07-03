package com.jsqix.camerarecord;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.os.storage.StorageManager;
import android.support.v7.app.AppCompatActivity;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Date;
import java.util.List;

public class ChooseActivity extends AppCompatActivity implements View.OnClickListener {

    private static String TAG = "ChooseActivity";
    private Button mBtnActivity;
    private Button mBtnStartService;
    private Button mBtnStopService;
    private boolean mServiceRunning;
    private Intent mServiceIntent;

    private StorageManager mStorageManager;
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
//                testSDcard(this);
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

    private void testSDcard(Context context) {
        String state = Environment.getExternalStorageState();
        Log.d(TAG,"getExternalStorageDirectory = "+Environment.getExternalStorageDirectory());
        Log.d(TAG,"getRootDirectory = "+Environment.getRootDirectory());
        Log.d(TAG,"getDataDirectory = "+Environment.getDataDirectory());
        String name = DateFormat.format("yyyy-MM-dd_kk-mm-ss",new Date().getTime())+".mp4";
        String path;
        if(Environment.MEDIA_MOUNTED.equals(state)) {
            path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES)+"/"+name;
        } else {
            path = getFilesDir().getAbsolutePath()+"/"+name;
        }
        //mStorageManager = (StorageManager) context.getSystemService(Context.STORAGE_SERVICE);
        String sdcardPath = getSdCardPath(context);
        Log.d(TAG,"sdcardPath="+sdcardPath);
    }
    private String getSdCardPath(Context context) {

        int TYPE_PUBLIC = 0;

        File file = null;

        String path = null;

        //StorageManager mStorageManager = getSystemService(StorageManager.class);
        mStorageManager = (StorageManager) context.getSystemService(Context.STORAGE_SERVICE);

        Class<?> mVolumeInfo = null;
        try {
            mVolumeInfo = Class.forName("android.os.storage.VolumeInfo");


            Method getVolumes = mStorageManager.getClass().getMethod(
                    "getVolumes");


            Method volType = mVolumeInfo.getMethod("getType");

            Method isMount = mVolumeInfo.getMethod("isMountedReadable");

            Method getPath = mVolumeInfo.getMethod("getPath");

            List<Object> mListVolumeinfo = (List<Object>) getVolumes
                    .invoke(mStorageManager);



            Log.d("getSdCardPath", "mListVolumeinfo.size="+mListVolumeinfo.size());

            for (int i = 0; i < mListVolumeinfo.size(); i++) {

                int mType = (Integer) volType.invoke(mListVolumeinfo.get(i));

                Log.d("getSdCardPath", "mType=" + mType);

                if (mType == TYPE_PUBLIC) {
                    boolean misMount = (Boolean) isMount.invoke(mListVolumeinfo.get(i));
                    Log.d("getSdCardPath", "misMount=" + misMount);
                    if (misMount) {
                        file = (File) getPath.invoke(mListVolumeinfo.get(i));
                        if (file != null) {
                            path = file.getAbsolutePath();
                            Log.d("getSdCardPath", "path=" + path);
                            return path;
                        }
                    }
                }

            }

        } catch (ClassNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return "";
    }
}
