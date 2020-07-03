package com.jsqix.camerarecord;

import android.app.Notification;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.media.CamcorderProfile;
import android.media.CameraProfile;
import android.media.MediaRecorder;
import android.os.Environment;
import android.os.IBinder;
import android.os.storage.StorageManager;
import android.util.Log;
import android.view.Gravity;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.WindowManager;

//import java.text.DateFormat;
import android.text.format.DateFormat;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Date;
import java.util.List;

public class BackgroundVideoRecorder extends Service implements SurfaceHolder.Callback {
    private final static String TAG = "bgRecSvc";

    private WindowManager windowManager;
    private SurfaceView surfaceView;

    private Camera camera = null;
    private MediaRecorder mediaRecorder = null;
    private final static int CAMERA_ID = 0;

    private boolean mIsRecording = false;
    private int MINUTE_TO_MS = 60*1000;
    private int HOUR_TO_MS = MINUTE_TO_MS*60;
    private int HOURS = 20;
    private int MINUTES = 1;
    //10*60*60*1000 -> 10 hours
    private int MAX_DURATION = HOURS*HOUR_TO_MS;
    //private int MAX_DURATION = MINUTES*MINUTE_TO_MS;

    private StorageManager mStorageManager;
    @Override
    public void onCreate() {
        Log.d(TAG,"onCreate");
        Notification notification = new Notification.Builder(this)
                                        .setContentTitle("Backgroud video recorder")
                                        .setContentText("")
                                        .setSmallIcon(R.mipmap.ic_launcher)
                                        .build();
        startForeground(1234,notification);

        windowManager = (WindowManager)this.getSystemService(Context.WINDOW_SERVICE);
        surfaceView = new SurfaceView(this);

        WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams(
                                                1,1,
                                                WindowManager.LayoutParams.TYPE_SYSTEM_OVERLAY,
                                                WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH,
                                                PixelFormat.TRANSLUCENT);
        layoutParams.gravity = Gravity.LEFT | Gravity.TOP;
        windowManager.addView(surfaceView,layoutParams);

        surfaceView.getHolder().addCallback(this);
    }

    @Override
    public void onDestroy() {
        Log.d(TAG,"onDestroy");
        if(mIsRecording == true) {
            stopRecord();
        }
        windowManager.removeView(surfaceView);
    }
    @Override
    public IBinder onBind(Intent intent) {
        Log.d(TAG,"who bind me "+intent);
        return null;
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        Log.d(TAG,"surfaceCreated holder="+holder);
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder,int format,int width,int height) {
        Log.d(TAG,"surfaceChanged w="+width+",h="+height+"format="+format+",holder="+holder);
        startRecord(holder);
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        Log.d(TAG,"surfaceDestroyed holder="+holder);
    }

    private void startRecord(SurfaceHolder holder) {
        Log.d(TAG,"start record");
        camera = Camera.open(CAMERA_ID);
        mediaRecorder = new MediaRecorder();

        camera.unlock();
        if(false) {

            mediaRecorder.setPreviewDisplay(holder.getSurface());
            mediaRecorder.setCamera(camera);
            mediaRecorder.setAudioSource(MediaRecorder.AudioSource.CAMCORDER);
            mediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);
            mediaRecorder.setProfile(CamcorderProfile.get(CamcorderProfile.QUALITY_720P));

        } else {

            mediaRecorder.setCamera(camera);
            mediaRecorder.setOrientationHint(90);  //改变保存后的视频文件播放时是否横屏(不加这句，视频文件播放的时候角度是反的)
            mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC); // 设置从麦克风采集声音
            mediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA); // 设置从摄像头采集图像
            mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);  // 设置视频的输出格式 为MP4
            mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.DEFAULT); // 设置音频的编码格式
            mediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264); // 设置视频的编码格式
            mediaRecorder.setVideoEncodingBitRate(3 * 1024 * 1024);// 设置视频编码的比特率
            //mRecorder.setVideoSize(1280, 720);  // 设置视频大小
            mediaRecorder.setVideoSize(1280, 720);  // 设置视频大小
            mediaRecorder.setVideoFrameRate(20); // 设置帧率
//        mRecorder.setMaxDuration(10000); //设置最大录像时间为10s
            mediaRecorder.setMaxDuration(MAX_DURATION);
            mediaRecorder.setOnInfoListener(mInfoListener);
            mediaRecorder.setPreviewDisplay(holder.getSurface());

        }
        String sdCardPath = getSdCardPath(this);

        String state = Environment.getExternalStorageState();
        String name = DateFormat.format("yyyy-MM-dd_kk-mm-ss",new Date().getTime())+".mp4";
        String path;
        if(sdCardPath != null && sdCardPath.length() > 0) {
            path = sdCardPath + "/"+Environment.DIRECTORY_MOVIES+"/"+name;
        }
        else {
            if (Environment.MEDIA_MOUNTED.equals(state)) {
                path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES) + "/" + name;
            } else {
                path = getFilesDir().getAbsolutePath() + "/" + name;
            }
        }
        Log.d(TAG,"outfile path="+path);
        mediaRecorder.setOutputFile(path);
        try {
            mediaRecorder.prepare();
        }catch (IOException e){
            e.printStackTrace();
        }
        mediaRecorder.start();
        mIsRecording = true;
    }

    private void stopRecord() {
        Log.d(TAG,"stop record");
        mediaRecorder.stop();
        mediaRecorder.reset();
        mediaRecorder.release();

        camera.lock();
        camera.release();
        mIsRecording = false;
    }
    private MediaRecorder.OnInfoListener mInfoListener = new MediaRecorder.OnInfoListener() {
        /**
         * Called to indicate an info or a warning during recording.
         *
         * @param mr   the MediaRecorder the info pertains to
         * @param what the type of info or warning that has occurred
         * <ul>
         * <li>{@link #MEDIA_RECORDER_INFO_UNKNOWN}
         * <li>{@link #MEDIA_RECORDER_INFO_MAX_DURATION_REACHED}
         * <li>{@link #MEDIA_RECORDER_INFO_MAX_FILESIZE_REACHED}
         * </ul>
         * @param extra   an extra code, specific to the info type
         */
        @Override
        public void onInfo(MediaRecorder mediaRecorder, int what, int extra) {
            if(what == MediaRecorder.MEDIA_RECORDER_INFO_MAX_DURATION_REACHED){
                Log.d(TAG,"max duration reached,try to stop record.");
                if (mIsRecording) {
                    stopRecord();
                    stopSelf();
                }
            }
        }
    };
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
