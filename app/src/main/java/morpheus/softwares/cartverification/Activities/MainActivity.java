package morpheus.softwares.cartverification.Activities;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import morpheus.softwares.cartverification.R;

public class MainActivity extends AppCompatActivity {
    CardView newItem, retrieveItem;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        newItem = findViewById(R.id.mainNewItem);
        retrieveItem = findViewById(R.id.mainRetrieveItem);

        newItem.setOnClickListener(view -> startActivity(new Intent(MainActivity.this, NewScanCodeActivity.class)));
        retrieveItem.setOnClickListener(view -> startActivity(new Intent(MainActivity.this, VerifyScanCodeActivity.class)));
    }
}