//package com.dynamsoft.cameraapp;
//
//import android.content.Context;
//import android.os.Build;
//import android.util.Log;
//import android.util.Size;
//
//import androidx.annotation.NonNull;
//import androidx.annotation.RequiresApi;
//import androidx.camera.core.ImageAnalysis;
//import androidx.camera.core.ImageProxy;
//import androidx.camera.lifecycle.ProcessCameraProvider;
//import androidx.core.content.ContextCompat;
//import androidx.lifecycle.LifecycleOwner;
//
//import java.util.LinkedList;
//import java.util.List;
//import java.util.concurrent.Executor;
//import java.util.concurrent.Executors;
//
//public class LayKhungHinh implements ImageAnalysis.Analyzer {
//    private List<byte[]> frameList = new LinkedList<>();
//    Executor executor = Executors.newSingleThreadExecutor();
//    private int a = 0;
//    private ProcessCameraProvider cameraProvider;
//    private ImageAnalysis imageAnalysis;
//    private LifecycleOwner lifecycleOwner;
//    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
//    public void imageAnalysis (ProcessCameraProvider cameraProvider){
//        cameraProvider.unbindAll();
//
//        ImageAnalysis imageAnalysis = new ImageAnalysis.Builder()
//                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
//                .setTargetResolution(new Size(480,640))
//                .build();
//        imageAnalysis.setAnalyzer(executor, this);
//
//        cameraProvider.bindToLifecycle(lifecycleOwner, imageAnalysis );
//    }
//
//    @Override
//    public void analyze(@NonNull ImageProxy image) {
//       a = a+1;
//       test();
//    }
//    public int test (){
//        Log.d("TAG", "Khung hinh thu : " + a);
//        return a;
//    }
//}
