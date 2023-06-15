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

import java.util.ArrayList;
import java.util.List;

import morpheus.softwares.cartverification.Models.Links;

public class NewScanCodeActivity extends AppCompatActivity {
    private final String JSONURL = new Links().getDATABASEURL();

    CodeScannerView scanView;
    private CodeScanner codeScanner;

    TextView scannedCode;
    ImageButton check, okay;
    EditText manualSN;

    FloatingActionButton fab;
    AlertDialog alertDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_scan_code);

        setUpPermission();

        check = findViewById(R.id.proceed);
        scannedCode = findViewById(R.id.code);
        scanView = findViewById(R.id.scannerView);
        fab = findViewById(R.id.scanEdit);

        codeScanner = new CodeScanner(this, scanView);
        codeScanner.setScanMode(ScanMode.CONTINUOUS);
        codeScanner.setFormats(CodeScanner.ALL_FORMATS);
        codeScanner.setAutoFocusMode(AutoFocusMode.SAFE);
        codeScanner.setAutoFocusEnabled(true);
        codeScanner.setFlashEnabled(false);

        codeScanner.setDecodeCallback(result -> runOnUiThread(() -> scannedCode.setText(result.getText())));
        codeScanner.setErrorCallback(result -> runOnUiThread(() -> Snackbar.make(findViewById(R.id.scanner), "Camera initialization error...", Snackbar.LENGTH_LONG).show()));

        scanView.setOnClickListener(v -> codeScanner.startPreview());

        check.setOnClickListener(v -> {
            String serialNumber = String.valueOf(scannedCode.getText()).trim();

            if (serialNumber.isEmpty() || serialNumber.contains(" ") || serialNumber.equals("null") || serialNumber.contains("NPC")) {
                scannedCode.setError("Please scan a code to check...");
            } else {
                checkSerials(serialNumber);
            }
        });

        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(NewScanCodeActivity.this, R.style.MaterialAlertDialogRounded);
        builder.setTitle("Input S/N");

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

        builder.setView(view);

        alertDialog = builder.create();
        alertDialog.setCanceledOnTouchOutside(false);

        fab.setOnClickListener(v -> alertDialog.show());
    }

    private void checkSerials(String serialNumber) {
        ProgressDialog dialog = ProgressDialog.show(this, "Cloud check", "Checking device serial number from cloud, please wait...");
        List<String> serials = new ArrayList<>();

        StringRequest request = new StringRequest(Request.Method.GET, JSONURL, response -> {

            try {
                JSONObject jsonObject = new JSONObject(response);
                JSONArray jsonArray = jsonObject.getJSONArray("items");

                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject object = jsonArray.getJSONObject(i);
                    String sn = object.getString("SN");
                    serials.add(sn);
                }
            } catch (JSONException e) {
                Snackbar.make(findViewById(R.id.scanner), "Encountered error while scanning " + serialNumber + " from cloud database, try again...", Snackbar.LENGTH_LONG).show();
                dialog.dismiss();
                e.printStackTrace();
            }

            if (serials.contains(serialNumber)) {
                dialog.dismiss();

                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setCancelable(false);
                builder.setTitle("Update?");
                builder.setIcon(R.drawable.ic_baseline_question_mark_24);
                builder.setInverseBackgroundForced(true);
                builder.setMessage(serialNumber + " already exists in cloud database...\nAre you trying to update device?").setPositiveButton("Yes", (alert, which) -> {
                    startActivity(new Intent(NewScanCodeActivity.this, NewItemActivity.class));
                    alert.dismiss();
                }).setNegativeButton("No", (alert, which) -> alert.cancel());
                AlertDialog alertDialog = builder.create();
                alertDialog.show();
            } else {
                Intent intent = new Intent(NewScanCodeActivity.this, NewItemActivity.class);
                intent.putExtra("deviceID", serialNumber);
                startActivity(intent);
                dialog.dismiss();
                finish();
            }
        }, error -> {
            Snackbar.make(findViewById(R.id.scanner), "Encountered error while checking for " + serialNumber + " in cloud database, try again...", Snackbar.LENGTH_LONG).show();
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
            Snackbar.make(findViewById(R.id.scanner), "Please grant Cart the permission to use camera...", Snackbar.LENGTH_LONG).show();
            int CAMERA_REQUEST_CODE = 101;
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, CAMERA_REQUEST_CODE);
        }
    }
}