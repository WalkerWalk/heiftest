package com.example.walkerxu.myapplication;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.media.MediaPlayer;
//import android.support.v7.app.ActionBar;
//import android.support.v7.app.AppCompatActivity;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.transition.Visibility;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.TextView;
import android.text.method.ScrollingMovementMethod;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;

import clarifai2.api.ClarifaiBuilder;
import clarifai2.api.ClarifaiClient;
import clarifai2.api.request.model.PredictRequest;
import clarifai2.dto.input.ClarifaiInput;
import clarifai2.dto.model.Model;
import clarifai2.dto.model.output.ClarifaiOutput;
import clarifai2.dto.prediction.Concept;
import clarifai2.dto.prediction.Frame;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
public class HeifPlayerActivity extends Activity {
    /**
     * Whether or not the system UI should be auto-hidden after
     * {@link #AUTO_HIDE_DELAY_MILLIS} milliseconds.
     */
    private static final boolean AUTO_HIDE = true;

    /**
     * If {@link #AUTO_HIDE} is set, the number of milliseconds to wait after
     * user interaction before hiding the system UI.
     */
    private static final int AUTO_HIDE_DELAY_MILLIS = 3000;

    /**
     * Some older devices needs a small delay between UI widget updates
     * and a change of the status and navigation bar.
     */
    private static final int UI_ANIMATION_DELAY = 300;

    private View mContentView;
    private View mControlsView;
    private boolean mVisible;
    private String tv;
    private SurfaceView mSurfaceView;
    private SurfaceHolder mSurfaceHolder;
    private boolean mImageTest;
    private boolean mVideoTest;
    private boolean mVideoPlaybackLoop;
    private MediaPlayer mVideoPlayer;
    private List<ClarifaiOutput<Frame>> mVideoResults;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_fullscreen);

        mVisible = true;
        mImageTest = true;
        mVideoTest = false;
        mVideoPlaybackLoop = false;
        mVideoPlayer = null;
        mVideoResults = null;
        mControlsView = findViewById(R.id.fullscreen_content_controls);
        mContentView = findViewById(R.id.fullscreen_content);
        mSurfaceView = (SurfaceView)findViewById(R.id.playbackview);
        mSurfaceHolder = mSurfaceView.getHolder();
        mSurfaceHolder.addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                Log.i("walkerxu", "surfaceCreated.");
                if(mImageTest) {
                    new Thread() {
                        @Override
                        public void run() {
//                            drawImg();
                        }
                    }.start();
                }
                if(mVideoTest) {
                    new Thread() {
                        @Override
                        public void run() {
//                            drawVideo();
                        }
                    }.start();
                }
            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
                Log.i("walkerxu", "surfaceChanged.");
            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {
                Log.i("walkerxu", "surfaceDestroyed.");
            }
        });
//        mSurfaceView.setVisibility(View.INVISIBLE);


        // Set up the user interaction to manually show or hide the system UI.
        mContentView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                toggle();
            }
        });

        mSurfaceView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                toggle();
            }
        });

        // Upon interacting with UI controls, delay any scheduled hide()
        // operations to prevent the jarring behavior of controls going away
        // while interacting with the UI.
        findViewById(R.id.dummy_button).setOnTouchListener(mDelayHideTouchListener);
        findViewById(R.id.dummy_button).setOnClickListener(mDummyClickListener);
        findViewById(R.id.dummy_button1).setOnClickListener(mDummy1ClickListener);
        findViewById(R.id.dummy_button2).setOnClickListener(mDummy2ClickListener);
        findViewById(R.id.dummy_button3).setOnClickListener(mDummy3ClickListener);
        findViewById(R.id.dummy_button4).setOnClickListener(mDummy4ClickListener);

        tv = new String();
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        // Trigger the initial hide() shortly after the activity has been
        // created, to briefly hint to the user that UI controls
        // are available.
//        delayedHide(100);
    }

    /**
     * Touch listener to use for in-layout UI controls to delay hiding the
     * system UI. This is to prevent the jarring behavior of controls going away
     * while interacting with activity UI.
     */
    private final View.OnTouchListener mDelayHideTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            if (AUTO_HIDE) {
 //               delayedHide(AUTO_HIDE_DELAY_MILLIS);
            }
            return false;
        }
    };

    private void drawVideo() {
        if(mVideoPlayer == null) {
            mVideoPlayer = new MediaPlayer();
        }
        try {
            mVideoPlayer.setSurface(mSurfaceHolder.getSurface());
            mVideoPlayer.setLooping(true);
            if(isLocalVideoExist()) {
                mVideoPlayer.setDataSource("/sdcard/aivideotest.mp4");
            } else {
                mVideoPlayer.setDataSource("https://samples.clarifai.com/beer.mp4");
            }
            mVideoPlayer.prepare();
            mVideoPlayer.start();
        }catch (Exception e) {
            e.printStackTrace();
        }

        while(mVideoPlaybackLoop) {
            if(mVideoPlayer != null && mVideoResults != null) {
                int playPosMs = mVideoPlayer.getCurrentPosition();
                int playIndex = playPosMs / 1000;
                tv = ("视频检测结果:\n");
//                for(int i = 0; i < videoResults.size(); i++) {
                    List<Frame> data = mVideoResults.get(0).data();
                Log.i("playindex","index:" + playIndex);
                if(playIndex < 0 || playIndex >= data.size()){
                    break;
                }
//                    for(int j = 0; j < data.size(); j++) {
                        Log.i("videotest", "frame:"+data.get(playIndex).index()+" timestamp:"+data.get(playIndex).time());
                        List<Concept> finaldata = data.get(playIndex).concepts();
                        tv += "######第"+data.get(playIndex).index()+"帧"+", 时间戳："+data.get(playIndex).time()+"ms\n";
                        for(int k = 0; k < finaldata.size(); k++) {
                            Log.i("videotest", "prediction:"+finaldata.get(k).name()+";"+finaldata.get(k).value()
                                    +";"+finaldata.get(k).language());
                            tv += "事物:"+finaldata.get(k).name()+"得分:"+finaldata.get(k).value()+"\n";
                        }
//                    }
//                }
                runOnUiThread(new Runnable() {
                    public void run() {
                        TextView textV = (TextView)mContentView;
                        textV.setMovementMethod(ScrollingMovementMethod.getInstance());
                        textV.setText(tv);
                    }
                });
                try{
                    Thread.sleep(500);
                }catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private boolean isLocalVideoExist() {
        try {
            File file = new File("/sdcard/aivideotest.mp4");
            if(file == null || !file.exists()) {
                return false;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    private void drawImg(){
        mVideoResults = null;
        if(mVideoPlayer != null) {
            mVideoPlayer.stop();
            mVideoPlayer.reset();
        }
        Canvas canvas = mSurfaceHolder.lockCanvas();
        if(canvas == null || mSurfaceHolder == null){
            return;
        }
        Bitmap bitmap  = null;
        try{
            String path = null;
            if(isLocalImageExist()) {
                path = "/sdcard/aiimagetest.jpg";
                bitmap  = BitmapFactory.decodeStream(new FileInputStream(new File("/sdcard/aiimagetest.jpg")));
            } else {
                path = "https://samples.clarifai.com/metro-north.jpg";
                bitmap  = BitmapFactory.decodeStream(getNetPic(path));
            }
            if(bitmap!=null){
                //画布宽和高
                int height = canvas.getHeight();
                int width  = canvas.getWidth();
                //生成合适的图像
                bitmap = getReduceBitmap(bitmap,width,height);
                Paint paint = new Paint();
                paint.setAntiAlias(true);
                paint.setStyle(Paint.Style.FILL);
                //清屏
                paint.setColor(Color.BLACK);
                canvas.drawRect(new Rect(0, 0, width,height), paint);
                //Log.d("ImageSurfaceView_IMG",path);
                //画图
                Matrix matrix = new Matrix();
                canvas.drawBitmap(bitmap, matrix, paint);
            }
            //解锁显示
            mSurfaceHolder.unlockCanvasAndPost(canvas);
        }catch(Exception ex){
            Log.e("walkerxu","pp"+ex.getStackTrace());
            ex.printStackTrace();
            return;
        }finally{
            //资源回收
            if(bitmap!=null){
                bitmap.recycle();
            }
        }
    }

    private boolean isLocalImageExist() {
        try {
            File file = new File("/sdcard/aiimagetest.jpg");
            if(file == null || !file.exists()) {
                return false;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    //缩放图片
    private Bitmap getReduceBitmap(Bitmap bitmap ,int w,int h){
        int     width     =     bitmap.getWidth();
        int     hight     =     bitmap.getHeight();
        Matrix     matrix     =     new Matrix();
        float     wScake     =     ((float)w/width);
        float     hScake     =     ((float)h/hight);
        matrix.postScale(wScake, hScake);
        return Bitmap.createBitmap(bitmap, 0,0,width,hight,matrix,true);
    }

    private InputStream getNetPic(String path) {
        int HttpResult;
//        String ee = new String();
        try
        {
            URL url =new URL(path); // 创建URL
            URLConnection urlconn = url.openConnection(); // 试图连接并取得返回状态码
            urlconn.connect();
            HttpURLConnection httpconn =(HttpURLConnection)urlconn;
            HttpResult = httpconn.getResponseCode();
            if(HttpResult != HttpURLConnection.HTTP_OK) // 不等于HTTP_OK说明连接不成功
                System.out.print("无法连接到");
            else
            {
                int filesize = urlconn.getContentLength(); // 取数据长度
//                InputStreamReader isReader = new InputStreamReader(urlconn.getInputStream());
//                BufferedReader reader = new BufferedReader(isReader);
//                StringBuffer buffer = new StringBuffer();
//                String line; // 用来保存每行读取的内容
//                line = reader.readLine(); // 读取第一行
//                while (line != null) { // 如果 line 为空说明读完了
//                    buffer.append(line); // 将读到的内容添加到 buffer 中
//                    buffer.append(" "); // 添加换行符
//                    line = reader.readLine(); // 读取下一行
//                }
//                System.out.print(buffer.toString());
//                ee = buffer.toString();
                return urlconn.getInputStream();
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private final View.OnClickListener mDummyClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view){
            mVideoPlaybackLoop = false;
            mImageTest = true;
            mVideoTest = false;
//            mSurfaceView.setVisibility(View.INVISIBLE);
//            mSurfaceView.setVisibility(View.VISIBLE);
            new Thread() {
                @Override
                public void run() {
//                    testClarifai(true, false);
                    heifwrapper hw = new heifwrapper();
                    hw.loadGridImage("/sdcard/grid_960x640.heic",mSurfaceHolder.getSurface());
                }
            }.start();
        }
    };

    private final View.OnClickListener mDummy1ClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view){
            mVideoTest = true;
            mVideoPlaybackLoop = true;
            mImageTest = false;
//            mSurfaceView.setVisibility(View.INVISIBLE);
//            mSurfaceView.setVisibility(View.VISIBLE);
            new Thread() {
                @Override
                public void run() {
//                    testClarifai(false, true);
                    new Thread() {
                        @Override
                        public void run() {
//                            drawVideo();
                            heifwrapper hw = new heifwrapper();
                            hw.loadSingleImage("/sdcard/old_bridge_1440x960.heic",mSurfaceHolder.getSurface());
                        }
                    }.start();
                }
            }.start();
        }
    };

    private final View.OnClickListener mDummy2ClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view){
            mVideoPlaybackLoop = false;
            mImageTest = true;
            mVideoTest = false;
//            mSurfaceView.setVisibility(View.INVISIBLE);
//            mSurfaceView.setVisibility(View.VISIBLE);
            new Thread() {
                @Override
                public void run() {
//                    testClarifai(true, false);
                    heifwrapper hw = new heifwrapper();
                    hw.loadSequenceImage("/sdcard/candle_animation.heic",mSurfaceHolder.getSurface());
                }
            }.start();
        }
    };

    private final View.OnClickListener mDummy3ClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view){
            mVideoPlaybackLoop = false;
            mImageTest = true;
            mVideoTest = false;
//            mSurfaceView.setVisibility(View.INVISIBLE);
//            mSurfaceView.setVisibility(View.VISIBLE);
            new Thread() {
                @Override
                public void run() {
//                    testClarifai(true, false);
                    heifwrapper hw = new heifwrapper();
                    hw.loadOverlayImage("/sdcard/alpha_1440x960.heic",mSurfaceHolder.getSurface());
                }
            }.start();
        }
    };

    private final View.OnClickListener mDummy4ClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view){
            mVideoPlaybackLoop = false;
            mImageTest = true;
            mVideoTest = false;
//            mSurfaceView.setVisibility(View.INVISIBLE);
//            mSurfaceView.setVisibility(View.VISIBLE);
            new Thread() {
                @Override
                public void run() {
//                    testClarifai(true, false);
//                    heifwrapper hw = new heifwrapper();
//                    hw.loadIdentityImage("/sdcard/alpha_1440x960.heic",mSurfaceHolder.getSurface());
                    Uri uri = heifonandroidp.testHeif2Jpeg("/storage/emulated/0/DCIM/heif/old_bridge_1440x960.heic", "/sdcard/Download/oooooolllldd.jpeg");
                    sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, uri));
                }
            }.start();
        }
    };

    private void testClarifai(boolean testimage, boolean testvideo) {
        //clinet object
        final ClarifaiClient client = new ClarifaiBuilder("bed937f45ff04ad5a56fc6d713252af2").buildSync();

        //image process
        if(testimage) {
            Model<Concept> generalModel = client.getDefaultModels().generalModel();
            ClarifaiInput input = null;
            if(isLocalImageExist()) {
                input = ClarifaiInput.forImage(new File("/sdcard/aiimagetest.jpg"));
            } else {
                input = ClarifaiInput.forImage("https://samples.clarifai.com/metro-north.jpg");
            }
            PredictRequest<Concept> request = generalModel.predict().withInputs(
                    input
            );
            List<ClarifaiOutput<Concept>> result = request.executeSync().get();
            Log.i("iamgetest", "results:" + result.size());
            tv = ("图像检测结果:\n");
            for(int i = 0; i < result.size(); i++) {
                List<Concept> data = result.get(i).data();
                for(int j = 0; j < data.size(); j++) {
                    Log.i("imagetest", "prediction:"+data.get(j).name()+";"+data.get(i).value()+";"+data.get(i).language());
                    tv += "事物:"+data.get(j).name()+"得分:"+data.get(j).value()+"\n";
                }
            }
            runOnUiThread(new Runnable() {
                public void run() {
                    TextView textV = (TextView)mContentView;
                    textV.setMovementMethod(ScrollingMovementMethod.getInstance());
                    textV.setText(tv);
                }
            });
        }

        //video process
        if(testvideo) {
            Model<Frame> generalVideoModel = client.getDefaultModels().generalVideoModel();
            ClarifaiInput input = null;
            if(isLocalVideoExist()) {
                input = ClarifaiInput.forVideo(new File("/sdcard/aivideotest.mp4"));
            } else {
                input = ClarifaiInput.forVideo("https://samples.clarifai.com/beer.mp4");
            }
            PredictRequest<Frame> videoRequest = generalVideoModel.predict().withInputs(
                    input
            );
            List<ClarifaiOutput<Frame>> videoResults = videoRequest.executeSync().get();
            mVideoResults = videoResults;
            Log.i("videotest", "results:" + videoResults.size());
            tv = ("视频检测结果:\n");
            for(int i = 0; i < videoResults.size(); i++) {
                List<Frame> data = videoResults.get(i).data();
                for(int j = 0; j < data.size(); j++) {
                    Log.i("videotest", "frame:"+data.get(j).index()+" timestamp:"+data.get(j).time());
                    List<Concept> finaldata = data.get(j).concepts();
                    tv += "######第"+data.get(j).index()+"帧"+", 时间戳："+data.get(j).time()+"ms\n";
                    for(int k = 0; k < finaldata.size(); k++) {
                        Log.i("videotest", "prediction:"+finaldata.get(k).name()+";"+finaldata.get(k).value()
                                +";"+finaldata.get(k).language());
                        tv += "事物:"+finaldata.get(k).name()+"得分:"+finaldata.get(k).value()+"\n";
                    }
                }
            }
            runOnUiThread(new Runnable() {
                public void run() {
                    TextView textV = (TextView)mContentView;
                    textV.setMovementMethod(ScrollingMovementMethod.getInstance());
                    textV.setText(tv);
                }
            });
        }
    }

    private void toggle() {
        if (mVisible) {
            hide();
        } else {
            show();
        }
    }

    private void hide() {
        // Hide UI first
//        ActionBar actionBar = getSupportActionBar();
//        if (actionBar != null) {
//            actionBar.hide();
//        }
        mControlsView.setVisibility(View.GONE);
        mVisible = false;

        // Schedule a runnable to remove the status and navigation bar after a delay
        mHideHandler.removeCallbacks(mShowPart2Runnable);
        mHideHandler.postDelayed(mHidePart2Runnable, UI_ANIMATION_DELAY);
    }

    private final Runnable mHidePart2Runnable = new Runnable() {
        @SuppressLint("InlinedApi")
        @Override
        public void run() {
            // Delayed removal of status and navigation bar

            // Note that some of these constants are new as of API 16 (Jelly Bean)
            // and API 19 (KitKat). It is safe to use them, as they are inlined
            // at compile-time and do nothing on earlier devices.
            mContentView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE
                    | View.SYSTEM_UI_FLAG_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
        }
    };

    @SuppressLint("InlinedApi")
    private void show() {
        // Show the system bar
        mContentView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);
        mVisible = true;

        // Schedule a runnable to display UI elements after a delay
        mHideHandler.removeCallbacks(mHidePart2Runnable);
        mHideHandler.postDelayed(mShowPart2Runnable, UI_ANIMATION_DELAY);
    }

    private final Runnable mShowPart2Runnable = new Runnable() {
        @Override
        public void run() {
            // Delayed display of UI elements
//            ActionBar actionBar = getSupportActionBar();
//            if (actionBar != null) {
//                actionBar.show();
//            }
            mControlsView.setVisibility(View.VISIBLE);
        }
    };

    private final Handler mHideHandler = new Handler();
    private final Runnable mHideRunnable = new Runnable() {
        @Override
        public void run() {
//            hide();
        }
    };

    /**
     * Schedules a call to hide() in [delay] milliseconds, canceling any
     * previously scheduled calls.
     */
    private void delayedHide(int delayMillis) {
        mHideHandler.removeCallbacks(mHideRunnable);
        mHideHandler.postDelayed(mHideRunnable, delayMillis);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mVideoPlaybackLoop = false;
        mVideoResults = null;
        if(mVideoPlayer != null) {
            mVideoPlayer.stop();
            mVideoPlayer.reset();
            mVideoPlayer.release();
            mVideoPlayer = null;
        }
    }
}
