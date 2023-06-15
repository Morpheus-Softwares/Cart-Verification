package morpheus.softwares.cartverification.Activities;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.RetryPolicy;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.snackbar.Snackbar;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import morpheus.softwares.cartverification.Models.Links;
import morpheus.softwares.cartverification.R;

public class VerifyActivity extends AppCompatActivity {
    private final String PRODUCTSURL = new Links().getPRODUCTSJSONURL();

    TextView idNum, serialNumber, productName, owner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verify);

        idNum = findViewById(R.id.verifyID);
        serialNumber = findViewById(R.id.verifySerialNumber);
        productName = findViewById(R.id.verifyProductName);
        owner = findViewById(R.id.verifyOwner);

        idNum.setText(String.valueOf(getIntent().getStringExtra("verifyID")));

        loadRecords();
    }

    private void loadRecords() {
        ProgressDialog dialog = ProgressDialog.show(this, "Cloud load", "Loading record from cloud database, please wait...");

        String code = String.valueOf(idNum.getText());

        StringRequest request = new StringRequest(Request.Method.GET, PRODUCTSURL, response -> {

            try {
                JSONObject jsonObject = new JSONObject(response);
                JSONArray jsonArray = jsonObject.getJSONArray("items");

                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject object = jsonArray.getJSONObject(i);
                    String id = object.getString("ID").trim();

                    if (id.equals(code)) {
                        productName.setText(object.getString("ProductName").trim());
                        serialNumber.setText(object.getString("SerialNumber").trim());
                        owner.setText(object.getString("Owner").trim());
                    }
                }
                dialog.dismiss();
            } catch (JSONException e) {
                Snackbar.make(findViewById(R.id.verify), "JSON error...", Snackbar.LENGTH_LONG).show();
                dialog.dismiss();
                e.printStackTrace();
            }
        }, error -> {
            Snackbar.make(findViewById(R.id.verify), String.valueOf(error.getMessage()), Snackbar.LENGTH_LONG).show();
            dialog.dismiss();
            error.printStackTrace();
        });

        int timeOut = 50000;
        RetryPolicy retryPolicy = new DefaultRetryPolicy(timeOut, 0, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        request.setRetryPolicy(retryPolicy);

        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(request);
    }
}