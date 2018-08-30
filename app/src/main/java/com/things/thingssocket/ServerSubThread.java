package com.things.thingssocket;

import android.os.Environment;
import android.util.Log;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Created by zzy on 2018/8/7 0007.
 * ---》zzy663785@163.com《---
 * Describe:通过new ServerSubThread.start()方法启动线程
 */
public class ServerSubThread  {

    private Socket connection = null;
    private ServerSocket serverSocket = null;
    private InputStream inputStream =null;
    public static String rec_data;

    private RecListener listener;

    ServerSubThread(RecListener listener) {
        this.listener = listener;
    }

    public void sendMessage(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
//            DataOutputStream outputStream = new DataOutputStream(connection.getOutputStream());
//            File file = new File("");
//            FileInputStream fis = new FileInputStream(file);
//            byte[] sendBytes = new byte[2048];
//            int length = 0;
//            while ((length = fis.read(sendBytes)) != -1){
//                outputStream.write(sendBytes,0,length);
//                outputStream.flush();
//            }
                    DataOutputStream dos = new DataOutputStream(connection.getOutputStream());
                    File file = new File(Environment.getExternalStorageDirectory()+"/Android/data/com.things.thingssocket/filespic.jpg");// 从该目录/sdcard/Android/data/com.things.thingssocket/filespic.jpg
                    if (file.exists()){
                        FileInputStream fis = new FileInputStream(file);
                        int size = fis.available();//当InputStream未进行read操作时available()的大小应该是等于文件大小的。但是在处理大文件时，后者会发生问题。
                        Log.i("123","size = "+size);
                        byte[] data = new byte[size];
                        fis.read(data);
                        dos.writeInt(size);
                        dos.write(data);
                        dos.flush();
//                        dos.close();
//                        connection.shutdownOutput();
                        fis.close();
                        Log.i("123","发送数据完毕");
                    }
                    else {
                        Log.i("123","文件目录不存在");
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();

    }

    public void creatServer(){

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    serverSocket = new ServerSocket(8086);
                    connection = serverSocket.accept();
                    Log.i("123", "device connected!!!!");
                    while (true){
                        try {
                                inputStream = connection.getInputStream();
                                Reader reader = new InputStreamReader(inputStream);
                                BufferedReader br = new BufferedReader(reader);
                                Log.i("123", "等待读取结果");
                                rec_data=null;
                                rec_data = br.readLine();//不输入数据时一直停留等待,而不是返回null,是一个阻塞函数.在数据流异常或断开时才会返回null
//                            while ((a = br.readLine()) != null) {
                                Log.i("123", "得到结果："+rec_data);
                                if (rec_data!=null){
                                    listener.getMessage();
                                }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

}
