package com.dynamsoft.cameraapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ExperimentalGetImage;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
import androidx.camera.core.VideoCapture;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import androidx.lifecycle.LifecycleOwner;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.ContentValues;

import android.content.pm.PackageManager;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.media.Image;

import android.os.Build;
import android.os.Bundle;

import android.provider.MediaStore;
import android.util.Log;
import android.util.Size;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.*;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.util.LinkedList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;

@ExperimentalGetImage
public class MainActivity extends AppCompatActivity implements ImageAnalysis.Analyzer, View.OnClickListener {
    private ListenableFuture<ProcessCameraProvider> cameraProviderFuture;
    private LinkedList<byte[]> frameList = new LinkedList<byte[]>();
    PreviewView previewView;
    ImageView imageView;
    CameraServer cameraServer;
    TextView textIP;
    byte[] lastFrame;
    private ImageCapture imageCapture;
    private VideoCapture videoCapture;
    private Button bRecord;
    private Button bCapture;

    @SuppressLint("NewApi")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        previewView = findViewById(R.id.previewView);
        //imageView = findViewById(R.id.imageView);
        textIP = findViewById(R.id.textIP);
        bCapture = findViewById(R.id.bCapture);
        bRecord = findViewById(R.id.bRecord);
        bRecord.setText("start recording"); // Set the initial text of the button

        bCapture.setOnClickListener(this);
        bRecord.setOnClickListener(this);
//        InetAddress mylocalhost = null;
//        try {
//            mylocalhost = InetAddress.getLocalHost();
//        } catch (UnknownHostException e) {
//            throw new RuntimeException(e);
//        }
//        textIP.setText(mylocalhost.getHostAddress()+"/"+mylocalhost.getHostName());
        cameraProviderFuture = ProcessCameraProvider.getInstance(this);
        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();
                startCameraX(cameraProvider);
                
                cameraServer = new CameraServer(this);
            } catch (ExecutionException | InterruptedException e) {

            }
        }, getExecutor());


    }

    Executor getExecutor() {
        return ContextCompat.getMainExecutor(this);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @SuppressLint("RestrictedApi")

    private void startCameraX(ProcessCameraProvider cameraProvider) {
        cameraProvider.unbindAll();
        CameraSelector cameraSelector = new CameraSelector.Builder()
                .requireLensFacing(CameraSelector.LENS_FACING_BACK)
                .build();
        Preview preview = new Preview.Builder()
                .build();
        preview.setSurfaceProvider(previewView.getSurfaceProvider());

        // Image capture use case
        imageCapture = new ImageCapture.Builder()
                .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                .build();
        // Video capture use case
        videoCapture = new VideoCapture.Builder()
                .setVideoFrameRate(30)
                .build();

        // Image analysis use case
        ImageAnalysis imageAnalysis = new ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build();

        imageAnalysis.setAnalyzer(getExecutor(), this);

        //bind to lifecycle:
        cameraProvider.bindToLifecycle((LifecycleOwner) this, cameraSelector, preview, imageCapture, imageAnalysis);
    }

    @SuppressLint("NewApi")
    @Override
    public void analyze(@NonNull ImageProxy imageProxy) {
        Image.Plane[] planes = imageProxy.getImage().getPlanes();

        ByteBuffer yBuffer = planes[0].getBuffer();
        ByteBuffer uBuffer = planes[2].getBuffer();
        ByteBuffer vBuffer = planes[1].getBuffer();

        int ySize = yBuffer.remaining();
        int uSize = uBuffer.remaining();
        int vSize = vBuffer.remaining();

        // Tạo mảng byte chứa toàn bộ dữ liệu pixel
//        byte[] data = new byte[ySize + uSize + vSize];
//        yBuffer.get(data, 0, ySize);
//        uBuffer.get(data, ySize, uSize);
//        vBuffer.get(data, ySize + uSize, vSize);
        int imageFormat = imageProxy.getFormat();
        Log.e("TAG", ""+ imageFormat);
        byte[] data = new byte[ySize + vSize + uSize];
        yBuffer.get(data, 0, ySize);
        uBuffer.get(data, ySize, vSize);
        vBuffer.get(data, ySize + vSize, uSize);
        // Tạo Bitmap từ dữ liệu pixel
        YuvImage yuvImage = new YuvImage(data, ImageFormat.NV21, imageProxy.getWidth(), imageProxy.getHeight(), null);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        yuvImage.compressToJpeg(new Rect(0, 0, imageProxy.getWidth(), imageProxy.getHeight()), 30, out);
        byte[] jpegData = out.toByteArray();
        //Bitmap bitmap = BitmapFactory.decodeByteArray(jpegData, 0, jpegData.length);
        //getByte(jpegData);

        frameList.add(jpegData);
        manageFrameList();
        //cameraServer = new CameraServer(getFrameList());
        imageProxy.close();
    }
    @SuppressLint("NewApi")
    private byte[] getDataFromImage(Image image) {
        Image.Plane[] planes = image.getPlanes();

        int totalSize = 0;
        for (Image.Plane plane : planes) {
            totalSize += plane.getBuffer().remaining();
        }

        byte[] data = new byte[totalSize];
        int offset = 0;

        for (Image.Plane plane : planes) {
            ByteBuffer buffer = plane.getBuffer();
            int size = buffer.remaining();
            buffer.get(data, offset, size);
            offset += size;
        }

        return data;
    }
    public void manageFrameList() {
        synchronized (frameList) {
            while (frameList.size() > 30) {
                frameList.poll();
            }
        }
    }
    public byte[] getImageBuffer() {
        synchronized (frameList) {
            if (frameList.size() > 0) {
                lastFrame = frameList.poll();
            }
        }
        return lastFrame;
    }
    public byte[] getByte (byte[] a){
       // Log.d("TAG", " Do dai  "+ a.length);
        return a ;
    }

    public LinkedList<byte[]> getFrameList(){
        Log.d("TAG", "So phan tu trong List: " + frameList.size() );
        return frameList;
    }

    private static final int MAX_FRAME_COUNT = 30; // Số lượng tối đa các khung hình trong danh sách

    @SuppressLint("RestrictedApi")
    private void recordVideo() {
        if (videoCapture != null) {

            long timestamp = System.currentTimeMillis();

            ContentValues contentValues = new ContentValues();
            contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, timestamp);
            contentValues.put(MediaStore.MediaColumns.MIME_TYPE, "video/mp4");

            try {
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                    return;
                }
                videoCapture.startRecording(
                        new VideoCapture.OutputFileOptions.Builder(
                                getContentResolver(),
                                MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                                contentValues
                        ).build(),
                        getExecutor(),
                        new VideoCapture.OnVideoSavedCallback() {
                            @Override
                            public void onVideoSaved(@NonNull VideoCapture.OutputFileResults outputFileResults) {
                                Toast.makeText(MainActivity.this, "Video has been saved successfully.", Toast.LENGTH_SHORT).show();
                            }

                            @Override
                            public void onError(int videoCaptureError, @NonNull String message, @Nullable Throwable cause) {
                                Toast.makeText(MainActivity.this, "Error saving video: " + message, Toast.LENGTH_SHORT).show();
                            }
                        }
                );
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    }

    private void capturePhoto() {
        long timestamp = System.currentTimeMillis();

        ContentValues contentValues = new ContentValues();
        contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, timestamp);
        contentValues.put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg");


        imageCapture.takePicture(
                new ImageCapture.OutputFileOptions.Builder(
                        getContentResolver(),
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                        contentValues
                ).build(),
                getExecutor(),
                new ImageCapture.OnImageSavedCallback() {
                    @Override
                    public void onImageSaved(@NonNull ImageCapture.OutputFileResults outputFileResults) {
                        Toast.makeText(MainActivity.this, "Photo has been saved successfully.", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onError(@NonNull ImageCaptureException exception) {
                        Toast.makeText(MainActivity.this, "Error saving photo: " + exception.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
        );

    }
    @SuppressLint("RestrictedApi")
    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.bCapture) {
            capturePhoto();
        } else if (view.getId() == R.id.bRecord) {
            // Handle the record case
            if (bRecord.getText() == "start recording"){
                bRecord.setText("stop recording");
                recordVideo();
            } else {
                bRecord.setText("start recording");
                videoCapture.stopRecording();
            }
        }

    }

}