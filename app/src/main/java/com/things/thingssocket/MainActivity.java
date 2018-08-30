package com.things.thingssocket;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.Image;
import android.media.ImageReader;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.util.Base64;
import android.util.Log;

import com.google.android.things.pio.Gpio;
import com.google.android.things.pio.PeripheralManager;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;

/**
 * Skeleton of an Android Things activity.
 * <p>
 * Android Things peripheral APIs are accessible through the class
 * PeripheralManagerService. For example, the snippet below will open a GPIO pin and
 * set it to HIGH:
 * <p>
 * <pre>{@code
 * PeripheralManagerService service = new PeripheralManagerService();
 * mLedGpio = service.openGpio("BCM6");
 * mLedGpio.setDirection(Gpio.DIRECTION_OUT_INITIALLY_LOW);
 * mLedGpio.setValue(true);
 * }</pre>
 * <p>
 * For more complex peripherals, look for an existing user-space driver, or implement one if none
 * is available.
 *
 * @see <a href="https://github.com/androidthings/contrib-drivers#readme">https://github.com/androidthings/contrib-drivers#readme</a>
 */
public class MainActivity extends Activity {

    private Gpio outN1;
    private Gpio outN2;
    private Gpio outN3;
    private  Gpio outN4;
    boolean takepicture=false;
    public static ServerSubThread subThread;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_main);
        GpioInit();
        initCamera();
        subThread =  new ServerSubThread(new RecListener() {
            @Override
            public void getMessage() {
                switch (ServerSubThread.rec_data){
                    case "forward":
                        try {
                            outN1.setValue(true);
                            outN2.setValue(false);
                            outN3.setValue(true);
                            outN4.setValue(false);
                        } catch (IOException e) {
                            e.printStackTrace();
                            Log.i("123","前进异常");
                        }
                        Log.i("123","前进");
                        break;
                    case "back":
                        try {
                            outN1.setValue(false);
                            outN2.setValue(true);
                            outN3.setValue(false);
                            outN4.setValue(true);
                        } catch (IOException e) {
                            e.printStackTrace();
                            Log.i("123","后退异常");
                        }
                        Log.i("123","后退");
                        break;
                    case "left":
                        try {
                            outN1.setValue(true);
                            outN2.setValue(false);
                            outN3.setValue(false);
                            outN4.setValue(false);
                        } catch (IOException e) {
                            e.printStackTrace();
                            Log.i("123","左转异常");
                        }
                        Log.i("123","左转");
                        break;
                    case "right":
                        try {
                            outN1.setValue(false);
                            outN2.setValue(false);
                            outN3.setValue(true);
                            outN4.setValue(false);
                        } catch (IOException e) {
                            e.printStackTrace();
                            Log.i("123","右转异常");
                        }
                        Log.i("123","右转");
                        break;
                    case "stop":
                        try {
                            outN1.setValue(false);
                            outN2.setValue(false);
                            outN3.setValue(false);
                            outN4.setValue(false);
                        } catch (IOException e) {
                            e.printStackTrace();
                            Log.i("123","停止异常");
                        }
                        Log.i("123","停止");
                        break;
                    case "takepicture":
//                        takePicture();
                        takepicture=true;
                        handler.sendEmptyMessage(0);
//                        Message msg = new Message();
//                        msg.obj = null;
//                        handler.sendMessage(msg);
                        break;
                    default:
                        try {
                            outN1.setValue(false);
                            outN2.setValue(false);
                            outN3.setValue(false);
                            outN4.setValue(false);
                        } catch (IOException e) {
                            e.printStackTrace();
                            Log.i("123","停止异常");
                        }
                        break;
                }
            }
        });
        subThread.creatServer();
//        while (true){
//            if (takepicture){
//                takePicture();
////                subThread.sendMessage();
//                }
//                takepicture=false;
//        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            outN1.close();
            outN2.close();
            outN3.close();
            outN4.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
//        mCamera.shutDown();
//        mCameraThread.quitSafely();
    }

    public void GpioInit(){
        PeripheralManager pio = PeripheralManager.getInstance();
        try {
            outN1 = pio.openGpio("BCM4");
            outN1.setDirection(Gpio.DIRECTION_OUT_INITIALLY_LOW);

            outN2 = pio.openGpio("BCM17");
            outN2.setDirection(Gpio.DIRECTION_OUT_INITIALLY_LOW);

            outN3 = pio.openGpio("BCM27");
            outN3.setDirection(Gpio.DIRECTION_OUT_INITIALLY_LOW);

            outN4 = pio.openGpio("BCM22");
            outN4.setDirection(Gpio.DIRECTION_OUT_INITIALLY_LOW);


        } catch (IOException e) {
            e.printStackTrace();
            Log.i("123","GPIO初始化异常");
        }
    }

    private DoorbellCamera mCamera;

    /**
     * A {@link Handler} for running Camera tasks in the background.
     */
    private Handler mCameraHandler;

    /**
     * Listener for new camera images.
     */
    private ImageReader.OnImageAvailableListener mOnImageAvailableListener =
            new ImageReader.OnImageAvailableListener() {
                @Override
                public void onImageAvailable(ImageReader reader) {
                    Log.d("123", "PhotoCamera OnImageAvailableListener");

                    Image image = reader.acquireLatestImage();
                    // get image bytes
                    ByteBuffer imageBuf = image.getPlanes()[0].getBuffer();
                    final byte[] imageBytes = new byte[imageBuf.remaining()];
                    imageBuf.get(imageBytes);
                    image.close();
                    onPictureTaken(imageBytes);
                }
            };

//    public int picVersion = 0;
    /**
     * Handle image processing in Firebase and Cloud Vision.
     */
    private void onPictureTaken(final byte[] imageBytes) {
        Log.d("123", "PhotoCamera onPictureTaken");
        if (imageBytes != null) {
            String imageStr = Base64.encodeToString(imageBytes, Base64.NO_WRAP | Base64.URL_SAFE);
            Log.d("123", "imageBase64:"+imageStr);

            final Bitmap bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
            if (bitmap != null) {
//                File file = new File(Environment.getDataDirectory()+"001.jpg");
//                File file=new File(getExternalFilesDir(null),"pic.jpg");//将要保存图片的路径
                try {
                    BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(getExternalFilesDir(null)+"pic.jpg"));// /sdcard/Android/data/com.things.thingssocket/filespic.jpg
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bos);
                    bos.flush();
                    bos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
//                picVersion++;
            }
        }
    }

    private HandlerThread mCameraThread;

    public void initCamera() {
        // We need permission to access the camera
        if (checkSelfPermission(Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            // A problem occurred auto-granting the permission
            Log.d("123", "PhotoCamera No permission");

            return;
        }

        //imageView = (ImageView)findViewById(R.id.imageView);

        DoorbellCamera.dumpFormatInfo(this);
        Log.d("123", "PhotoCamera inited");

        // Creates new handlers and associated threads for camera and networking operations.
        mCameraThread = new HandlerThread("CameraBackground");
        mCameraThread.start();
        mCameraHandler = new Handler(mCameraThread.getLooper());

        // Camera code is complicated, so we've shoved it all in this closet class for you.
        mCamera = DoorbellCamera.getInstance();
        mCamera.initializeCamera(this, mCameraHandler, mOnImageAvailableListener);

        /*imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.i("takePicture", "click image to take picture");
                mCamera.takePicture();
            }
        });*/
    }

    Handler handler = new Handler()
    {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what){
                case 0:
                    takePicture();
                break;
            }
        }
    };

    public void takePicture() {

        handler.post(new Runnable() {
            @Override
            public void run() {
                try {
                    Log.i("123", "PhotoCamera take Picture");
                    mCamera.takePicture();
                } catch (Exception e) {
                    e.printStackTrace();
                    Log.i("123", "异常  PhotoCamera take Picture");
                }
            }
        });
    }

}
