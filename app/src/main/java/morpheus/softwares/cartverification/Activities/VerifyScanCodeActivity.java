package morpheus.softwares.cartverification.Activities;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.RetryPolicy;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.budiyev.android.codescanner.AutoFocusMode;
import com.budiyev.android.codescanner.CodeScanner;
import com.budiyev.android.codescanner.CodeScannerView;
import com.budiyev.android.codescanner.ScanMode;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import morpheus.softwares.cartverification.Models.Links;
import morpheus.softwares.cartverification.R;

public class VerifyScanCodeActivity extends AppCompatActivity {
    private final String DATABASEURL = new Links().getDATABASEURL();

    CodeScannerView scanView;
    TextView scannedCode;
    ImageButton check, okay;
    EditText manualSN;
    FloatingActionButton fab;
    AlertDialog alertDialog;
    private CodeScanner codeScanner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verify_scan_code);

        setUpPermission();

        check = findViewById(R.id.retrieveCheck);
        scannedCode = findViewById(R.id.retrieveScannedCode);
        scanView = findViewById(R.id.retrieveScanView);
        fab = findViewById(R.id.retrieveCheckEdit);

        codeScanner = new CodeScanner(this, scanView);
        codeScanner.setScanMode(ScanMode.CONTINUOUS);
        codeScanner.setFormats(CodeScanner.ALL_FORMATS);
        codeScanner.setAutoFocusMode(AutoFocusMode.SAFE);
        codeScanner.setAutoFocusEnabled(true);
        codeScanner.setFlashEnabled(false);

        codeScanner.setDecodeCallback(result -> runOnUiThread(() -> scannedCode.setText(result.getText())));
        codeScanner.setErrorCallback(result -> runOnUiThread(() -> Snackbar.make(findViewById(R.id.verifyChecked), "Camera initialization error...", Snackbar.LENGTH_LONG).show()));

        scanView.setOnClickListener(v -> codeScanner.startPreview());

        check.setOnClickListener(v -> {
            if (String.valueOf(scannedCode.getText()).isEmpty()) {
                scannedCode.setError("Please scan a code to check...");
            } else {
                fetchRecords();
            }
        });

        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(VerifyScanCodeActivity.this, R.style.MaterialAlertDialogRounded);
        builder.setTitle("Input S/N");

        // Inflate custom_dialog
        View view = getLayoutInflater().inflate(R.layout.custom_dialog, null);
        manualSN = view.findViewById(R.id.manualSN);
        okay = view.findViewById(R.id.okay);

        okay.setOnClickListener(vi -> {
            if (String.valueOf(manualSN.getText()).isEmpty()) {
                manualSN.setError("Please scan a code to check...");
            } else if (String.valueOf(manualSN.getText()).contains(" ")) {
                manualSN.setError("Code shouldn't contain white spaces...");
            } else {
                scannedCode.setText(String.valueOf(manualSN.getText()));
                alertDialog.dismiss();
            }
        });

        // Set view to dialog
        builder.setView(view);

        // Create dialog
        alertDialog = builder.create();
        alertDialog.setCanceledOnTouchOutside(false);

        fab.setOnClickListener(v -> alertDialog.show());
    }

    private void fetchRecords() {
        ProgressDialog dialog = ProgressDialog.show(this, "Cloud fetch", "Fetching device serial number from cloud, please wait...");

        String serialNumber = String.valueOf(scannedCode.getText()).trim();

        StringRequest request = new StringRequest(Request.Method.GET, DATABASEURL, response -> {

            try {
                JSONObject jsonObject = new JSONObject(response);
                JSONArray jsonArray = jsonObject.getJSONArray("items");

                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject object = jsonArray.getJSONObject(i);
                    String id = object.getString("ID");

                    if (id.equals(serialNumber)) {
                        Intent intent = new Intent(VerifyScanCodeActivity.this, VerifyActivity.class);
                        intent.putExtra("verifyID", scannedCode.getText());
                        startActivity(intent);
                        dialog.dismiss();
                        finish();
                    } else {
                        Snackbar.make(findViewById(R.id.verifyChecked), serialNumber + " not found in cloud database...", Snackbar.LENGTH_LONG).show();
                        dialog.dismiss();
                    }
                }
            } catch (JSONException e) {
                Snackbar.make(findViewById(R.id.verifyChecked), "Encountered error while checking for " + serialNumber + " in cloud database, try again...", Snackbar.LENGTH_LONG).show();
                dialog.dismiss();
                e.printStackTrace();
            }
        }, error -> {
            Snackbar.make(findViewById(R.id.verifyChecked), "Encountered error while checking for " + serialNumber + " in cloud database, try again...", Snackbar.LENGTH_LONG).show();
            dialog.dismiss();
            error.printStackTrace();
        });

        int timeOut = 50000;
        RetryPolicy retryPolicy = new DefaultRetryPolicy(timeOut, 0, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        request.setRetryPolicy(retryPolicy);

        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(request);
    }

    @Override
    protected void onResume() {
        super.onResume();
        codeScanner.startPreview();
    }

    @Override
    protected void onPause() {
        codeScanner.releaseResources();
        super.onPause();
    }

    private void setUpPermission() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            Snackbar.make(findViewById(R.id.verifyChecked), "Please grant Cart the permission to use camera...", Snackbar.LENGTH_LONG).show();
            int CAMERA_REQUEST_CODE = 101;
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, CAMERA_REQUEST_CODE);
        }
    }
}