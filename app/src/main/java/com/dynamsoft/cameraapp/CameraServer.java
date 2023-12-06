package com.dynamsoft.cameraapp;

import android.annotation.SuppressLint;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;


public class CameraServer extends Thread {
    int i = 0;
    private ServerSocket server;
    MainActivity mActivity;
    private static final String TAG = "socket";
    private String ip = "192.168.5.240";
    byte[] lastFrame ;
    private LinkedList<byte[]> frameList = new LinkedList<byte[]>();
    private int port = 8888;
    //LayKhungHinh layKhungHinh;
    public CameraServer(MainActivity activity){
        mActivity = activity;
        start();
    }
    @SuppressLint("UnsafeOptInUsageError")
    public void run (){
        //super.run();
        Socket socket;
        BufferedOutputStream outputStream;
        BufferedInputStream inputStream;
        DataOutputStream out;

        try {
            server = new ServerSocket(9213);

            while (true) {
                try {
                    socket = server.accept();

                    while (true){
                        outputStream = new BufferedOutputStream(socket.getOutputStream());
//                        inputStream = new BufferedInputStream(socket.getInputStream());
//                        out = new DataOutputStream(socket.getOutputStream());
                        byte[] dataToSend = mActivity.getImageBuffer();
                        outputStream.write(dataToSend);
                        outputStream.flush();
                        Log.e("TAG", "Da gui mang voi kich thuoc :"+dataToSend.length);
                        Thread.sleep(45);
                    }
                } catch (Exception e1){

                }


            }
        } catch (Exception e){
            Log.e("TAG", "");

        }

    }



}



