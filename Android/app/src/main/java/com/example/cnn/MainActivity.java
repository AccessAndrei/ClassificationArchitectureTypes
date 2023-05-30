package com.example.cnn;

import static android.content.ContentValues.TAG;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.content.ContextCompat;
import android.Manifest;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.media.ThumbnailUtils;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;
import com.example.cnn.databinding.ActivityMainBinding;
import com.example.cnn.ml.Model;
import com.google.common.util.concurrent.ListenableFuture;

import org.tensorflow.lite.DataType;
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.concurrent.ExecutionException;
public class MainActivity extends AppCompatActivity {
    private ActivityMainBinding binding;
    private ListenableFuture<ProcessCameraProvider> cameraProviderFuture;
    private PreviewView previewView;
    private ImageCapture imageCapture;
    private ActivityResultLauncher<PickVisualMediaRequest> pickVisualLauncher;

    private TextView textView;
    private TextView newId;
    private void openCamera(){
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED){
            checkAllPermissions();
        }
        else {
            bindPreview();
        }
    }
    private void checkAllPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityResultLauncher<String[]> launcher = registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), result -> {
                result.forEach((permission, res) -> {
                    if (permission.equals((Manifest.permission.CAMERA))) {
                        openCamera();
                        bindPreview();
                    }
                });
            });
            launcher.launch(new String[]{Manifest.permission.CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE});
        }
    }

    private void bindPreview() {
        Preview preview = new Preview.Builder().build();
        CameraSelector cameraSelector = new CameraSelector.Builder()
                .requireLensFacing(CameraSelector.LENS_FACING_BACK)
                .build();

        imageCapture = new ImageCapture.Builder().build();
        cameraProviderFuture = ProcessCameraProvider.getInstance(this);
        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();


                preview.setSurfaceProvider(binding.cameraView.getSurfaceProvider());

                cameraProvider.unbindAll();
                cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageCapture);
            } catch (ExecutionException | InterruptedException e) {
                e.printStackTrace();
            }
        }, ContextCompat.getMainExecutor(this));
    }

    private void registerActivityForPickImage() {
        pickVisualLauncher = registerForActivityResult(new ActivityResultContracts.PickVisualMedia(), uri ->{
            if (uri != null){
                Log.d("PhotoPicker", "Selected URI: " + uri);
            }
            else {
                Log.d("PhotoPicker", "No media Selected");
            }
        });
        binding.galleryView.setOnClickListener(v -> {
            pickVisualLauncher.launch(new PickVisualMediaRequest.Builder()
                   .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE).build());
       });
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        previewView = findViewById(R.id.cameraView);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        textView = binding.textView;
        setContentView((binding.getRoot()));
        checkAllPermissions();
        newId = findViewById(R.id.classInfo);
        openCamera();
        registerActivityForPickImage();

        binding.takePicture.setOnClickListener(v -> {
            String name = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US).format(System.currentTimeMillis());
            ContentValues contentValues = new ContentValues();
            contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, name);
            contentValues.put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg");
            contentValues.put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/CNN-Images");

            ImageCapture.OutputFileOptions outputOptions =
                    new ImageCapture.OutputFileOptions.Builder(getContentResolver(),
                            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                            contentValues).build();
            imageCapture.takePicture(outputOptions, ContextCompat.getMainExecutor(this), new ImageCapture.OnImageSavedCallback() {
                @Override
                public void onImageSaved(@NonNull ImageCapture.OutputFileResults outputFileResults) {
                    String text = "Success!";
                    Toast.makeText(getBaseContext(), text, Toast.LENGTH_SHORT).show();
                    Log.d(TAG, text);
                    setImage();
                }

                @Override
                public void onError(@NonNull ImageCaptureException exception) {
                    String text = "Error: " + exception.getLocalizedMessage();
                    Toast.makeText(getBaseContext(), text, Toast.LENGTH_SHORT).show();
                    Log.d(TAG, text);
                }
            });
        });
        setContentView(binding.getRoot());
        bindPreview();
    }
    private String setImage() {
        Bitmap pixel = (Bitmap) binding.cameraView.getBitmap();
        if (pixel != null) {
            int dimension = Math.min(pixel.getWidth(), pixel.getHeight());
            pixel = ThumbnailUtils.extractThumbnail(pixel, dimension, dimension);
            pixel = Bitmap.createScaledBitmap(pixel,250, 250, false );
            return classifyImage(pixel);
        }
        return "";
    }

    private String classifyImage(Bitmap image) {
        String[] classes = new String[0];
        int maxPos = 0;
        try {
            Model model = Model.newInstance(getApplicationContext());

            // Creates inputs for reference.
            TensorBuffer inputFeature0 = TensorBuffer.createFixedSize(new int[]{1, 250, 250, 3}, DataType.FLOAT32);
            ByteBuffer byteBuffer = ByteBuffer.allocateDirect(4 * 250 * 250 * 3);
            byteBuffer.order(ByteOrder.nativeOrder());

            int[] values = new int[250 * 250];
            image.getPixels(values, 0, image.getWidth(), 0, 0, image.getWidth(), image.getHeight());
            int pixel = 0;
            for (int i = 0; i < 250; i++){
                for (int j = 0; j < 250; j++){
                    int val = values[pixel++];
                    byteBuffer.putFloat(((val >> 16) & 0xFF) * (1.f / 255.f));
                    byteBuffer.putFloat(((val >> 8) & 0xFF) * (1.f / 255.f));
                    byteBuffer.putFloat((val & 0xFF) * (1.f / 255.f));
                }
            }

            inputFeature0.loadBuffer(byteBuffer);

            Model.Outputs outputs = model.process(inputFeature0);
            TensorBuffer outputFeature0 = outputs.getOutputFeature0AsTensorBuffer();

            float[] confidences = outputFeature0.getFloatArray();
            maxPos = 0;
            float maxConfidence = 0;
            for (int i = 0; i < confidences.length; i++) {
                if (confidences[i] > maxConfidence){
                    maxConfidence = confidences[i];
                    maxPos = i;
                }
            }
            Log.e(TAG, String.valueOf(maxPos));
            classes = new String[] {"Modernism", "Classicism", "Deconstructivism", "Deconstructivizm_2", "Gothic", "Baroque"};
            Intent intent = new Intent(MainActivity.this, ResultActivity.class);
            intent.putExtra("Sasha", classes[maxPos]);
            startActivityForResult(intent, 5);
            // Releases model resources if no longer used.
            model.close();
        } catch (IOException e) {
            // TODO Handle the exception
        }

        return classes[maxPos];
    }
}