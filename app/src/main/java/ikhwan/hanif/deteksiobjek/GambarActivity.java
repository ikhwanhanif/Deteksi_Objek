package ikhwan.hanif.deteksiobjek;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import ikhwan.hanif.deteksiobjek.ml.SsdMobilenetV11Metadata1;

import org.tensorflow.lite.support.common.FileUtil;
import org.tensorflow.lite.support.image.ImageProcessor;
import org.tensorflow.lite.support.image.TensorImage;
import org.tensorflow.lite.support.image.ops.ResizeOp;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class GambarActivity extends AppCompatActivity {

    private Paint paint;
    private Button btn;
    private ImageView imageView;
    private Bitmap bitmap;
    private List<Integer> colors;
    private List<String> labels;
    private SsdMobilenetV11Metadata1 model;
    private ImageProcessor imageProcessor;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gambar);

        imageProcessor = new ImageProcessor.Builder().add(new ResizeOp(300, 300, ResizeOp.ResizeMethod.BILINEAR)).build();

        paint = new Paint();
        btn = findViewById(R.id.btn);
        imageView = findViewById(R.id.imaegView);

        colors = Arrays.asList(Color.BLUE, Color.GREEN, Color.RED, Color.CYAN, Color.GRAY, Color.BLACK,
                Color.DKGRAY, Color.MAGENTA, Color.YELLOW, Color.RED);

        try {
            labels = FileUtil.loadLabels(this, "labels.txt");
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            model = SsdMobilenetV11Metadata1.newInstance(this);
        } catch (IOException e) {
            e.printStackTrace();
        }

        paint.setColor(Color.BLUE);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(5.0f);

        Log.d("labels", labels.toString());

        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");

        btn.setOnClickListener(v -> startActivityForResult(intent, 101));
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 101) {
            Uri uri = data != null ? data.getData() : null;
            try {
                bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
                getPredictions();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        model.close();
    }

    private void getPredictions() {
        TensorImage image = TensorImage.fromBitmap(bitmap);
        image = imageProcessor.process(image);
        SsdMobilenetV11Metadata1.Outputs outputs = model.process(image);

        float[] locations = outputs.getLocationsAsTensorBuffer().getFloatArray();
        float[] classes = outputs.getClassesAsTensorBuffer().getFloatArray();
        float[] scores = outputs.getScoresAsTensorBuffer().getFloatArray();
        float[] numberOfDetections = outputs.getNumberOfDetectionsAsTensorBuffer().getFloatArray();

        Bitmap mutable = bitmap.copy(Bitmap.Config.ARGB_8888, true);
        Canvas canvas = new Canvas(mutable);
        int h = mutable.getHeight();
        int w = mutable.getWidth();
        paint.setTextSize(h / 15f);
        paint.setStrokeWidth(h / 85f);

        
        for (int index = 0; index < scores.length; index++) {
            if (scores[index] > 0.5) {
                int x = index * 4;
                paint.setColor(colors.get(index));
                paint.setStyle(Paint.Style.STROKE);
                canvas.drawRect(new RectF(locations[x + 1] * w, locations[x] * h, locations[x + 3] * w,
                        locations[x + 2] * h), paint);
                paint.setStyle(Paint.Style.FILL);
                canvas.drawText(labels.get((int) classes[index]) + " " + scores[index], locations[x + 1] * w,
                        locations[x] * h, paint);
            }
        }
        imageView.setImageBitmap(mutable);
    }
}
