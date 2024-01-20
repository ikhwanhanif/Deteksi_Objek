package ikhwan.hanif.deteksiobjek;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {

    Button scrLangsungBtn, scrGambarBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        scrLangsungBtn = findViewById(R.id.secaraLangsungBtn);
        scrGambarBtn = findViewById(R.id.secaraGambarBtn);

        scrLangsungBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivity.this, LangsungActivity.class));
            }
        });
        scrGambarBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivity.this, GambarActivity.class));
            }
        });


    }
}