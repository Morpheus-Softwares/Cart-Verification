package morpheus.softwares.cartverification.Activities;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.RetryPolicy;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import java.util.HashMap;
import java.util.Map;

import morpheus.softwares.cartverification.Models.Links;
import morpheus.softwares.cartverification.R;

public class NewItemActivity extends AppCompatActivity {
    private final String DATABASEURL = new Links().getDATABASEURL();

    TextView idNum;

    EditText serialNumber, productName, owner;

    FloatingActionButton fab;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_item);

        idNum = findViewById(R.id.newSerialNumber);
        serialNumber = findViewById(R.id.verifyDeviceSN);
        productName = findViewById(R.id.verifyProductName);
        owner = findViewById(R.id.verifyOwner);

        idNum.setText(String.valueOf(getIntent().getStringExtra("deviceID")));

        fab = findViewById(R.id.newSync);
        fab.setOnClickListener(view -> sync());
    }

    private void sync() {
        final ProgressDialog dialog = ProgressDialog.show(this, "Cloud sync", "Synchronizing records to cloud, please wait...");

        String sn = String.valueOf(serialNumber.getText()).trim();
        String name = String.valueOf(productName.getText()).trim();
        String id = String.valueOf(idNum.getText()).trim();
        String deviceOwner = String.valueOf(owner.getText()).trim();

        if (id.isEmpty()) {
            idNum.setError("Provide device ID");
            dialog.dismiss();
        } else if (sn.isEmpty()) {
            serialNumber.setError("Provide serial number");
            dialog.dismiss();
        } else if (name.isEmpty()) {
            productName.setError("Select office");
            dialog.dismiss();
        } else if (deviceOwner.isEmpty()) {
            owner.setError("Provide staff name");
            dialog.dismiss();
        } else {
            StringRequest request = new StringRequest(Request.Method.POST, DATABASEURL, response -> {
                dialog.dismiss();
                Snackbar.make(findViewById(R.id.newItem), response, Snackbar.LENGTH_LONG).show();
            }, error -> dialog.dismiss()) {
                @NonNull
                @Override
                protected Map<String, String> getParams() {
                    Map<String, String> params = new HashMap<>();
                    params.put("action", "Products");
                    params.put("ID", id);
                    params.put("SerialNumber", sn);
                    params.put("ProductName", name);
                    params.put("Owner", deviceOwner);
                    return params;
                }
            };

            int timeOut = 50000;
            RetryPolicy retryPolicy = new DefaultRetryPolicy(timeOut, 0, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
            request.setRetryPolicy(retryPolicy);

            RequestQueue requestQueue = Volley.newRequestQueue(this);
            requestQueue.add(request);
        }
    }
}