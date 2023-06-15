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
import android.widget.Toast;

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
import java.util.Arrays;
import java.util.Scanner;

import morpheus.softwares.cartverification.Models.Database;
import morpheus.softwares.cartverification.Models.Links;
import morpheus.softwares.cartverification.Models.Products;
import morpheus.softwares.cartverification.R;

public class VerifyScanCodeActivity extends AppCompatActivity {
    private final String DATABASEURL = new Links().getIDSJSONURL();
    Database database;

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

        database = new Database(VerifyScanCodeActivity.this);

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
//                fetchProductID();
//                fetchProductIDs();
                fetchProductIDNum();
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

    /**
     * Offline mode with text
     */
    private void fetchProductIDNum() {
        ProgressDialog dialog = ProgressDialog.show(this, "Cloud fetch", "Fetching device serial number from cloud, please wait...");

        String idNumber = String.valueOf(scannedCode.getText()).trim();
        ArrayList<String> foundItems = searchRow(idNumber);

        if (isInDatabase(idNumber)) {
            dialog.dismiss();

            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setCancelable(false);
            builder.setTitle("Verified");
            builder.setIcon(R.drawable.ic_baseline_check_24);
            builder.setInverseBackgroundForced(true);
            builder.setMessage(idNumber + " exists in cloud database..." +
                    "\nID: " + foundItems.get(0) +
                    "\nProduct Name: " + foundItems.get(1) +
                    "\nSerial Number: " + foundItems.get(2) +
                    "\nOwner: " + foundItems.get(3) +
                    "\nPrice: " + foundItems.get(4) +
                    "\nDate: " + foundItems.get(5)).setPositiveButton("Ok",
                    (alert, which) -> alert.dismiss()).setNegativeButton("Cancel", (alert, which) -> alert.cancel());
            AlertDialog alertDialog = builder.create();
            alertDialog.show();
        } else {
            Toast.makeText(getApplicationContext(), idNumber + " not in cloud database!",
                    Toast.LENGTH_SHORT).show();
            startActivity(new Intent(VerifyScanCodeActivity.this, NewScanCodeActivity.class));
            dialog.dismiss();
            finish();
        }
    }

    /**
     * Offline mode
     */
    private void fetchProductID() {
        ProgressDialog dialog = ProgressDialog.show(this, "Cloud fetch", "Fetching device serial number from cloud, please wait...");

        int idNumber = Integer.parseInt(String.valueOf(scannedCode.getText()).trim());

        ArrayList<Products> products = database.selectAllProducts();

        for (Products product : products) {
            if (product.getId() == idNumber) {
                dialog.dismiss();

                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setCancelable(false);
                builder.setTitle("Verified");
                builder.setIcon(R.drawable.ic_baseline_check_24);
                builder.setInverseBackgroundForced(true);
                builder.setMessage(idNumber + " exists in cloud database..." +
                        "\nID: " + product.getId() +
                        "\nProduct Name: " + product.getProductName() +
                        "\nSerial Number: " + product.getSerialNumber() +
                        "\nOwner: " + product.getOwner() +
                        "\nPrice: " + product.getPrice() +
                        "\nDate: " + product.getDate()).setPositiveButton("Ok",
                        (alert, which) -> alert.dismiss()).setNegativeButton("Cancel", (alert, which) -> alert.cancel());
                AlertDialog alertDialog = builder.create();
                alertDialog.show();
            } else {
                Toast.makeText(getApplicationContext(), idNumber + " not in cloud database!",
                        Toast.LENGTH_SHORT).show();
                startActivity(new Intent(VerifyScanCodeActivity.this, NewScanCodeActivity.class));
                dialog.dismiss();
                finish();
            }
        }
    }

    /**
     * Online mode
     */
    private void fetchIDs() {
        ProgressDialog dialog = ProgressDialog.show(this, "Cloud fetch", "Fetching device serial number from cloud, please wait...");

        String idNumber = String.valueOf(scannedCode.getText()).trim();

        StringRequest request = new StringRequest(Request.Method.GET, DATABASEURL, response -> {

            try {
                JSONObject jsonObject = new JSONObject(response);
                JSONArray jsonArray = jsonObject.getJSONArray("items");

                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject object = jsonArray.getJSONObject(i);
                    String id = object.getString("ID");

                    if (id.equals(idNumber)) {
                        Intent intent = new Intent(VerifyScanCodeActivity.this, VerifyActivity.class);
                        intent.putExtra("verifyID", scannedCode.getText());
                        startActivity(intent);
                        dialog.dismiss();
                        finish();
                    } else {
                        Snackbar.make(findViewById(R.id.verifyChecked), idNumber + " not found in cloud database...", Snackbar.LENGTH_LONG).show();
                        dialog.dismiss();
                    }
                }
            } catch (JSONException e) {
                Snackbar.make(findViewById(R.id.verifyChecked), "Encountered error while checking for " + idNumber + " in cloud database, try again...", Snackbar.LENGTH_LONG).show();
                dialog.dismiss();
                e.printStackTrace();
            }
        }, error -> {
            Snackbar.make(findViewById(R.id.verifyChecked), "Encountered error while checking for " + idNumber + " in cloud database, try again...", Snackbar.LENGTH_LONG).show();
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

    /**
     * Searches if a product is in the database
     */
    public boolean isInDatabase(String searchValue) {
        int i = 0;
        try (Scanner scanner =
                     new Scanner(getResources().openRawResource(R.raw.products))) {
            String line;
            while ((line = scanner.nextLine()) != null) {
                String[] values = line.split(",");
                // System.out.println("Found matching value in line: " + line);
                if (values.length > 0 && values[i].equals(searchValue)){
                    return true;
                }
                i++;
            }
        }

        return false;
    }

    /**
     * Searches for a product in database and returns the records in the row
     */
    private ArrayList<String> searchRow(String searchValue) {
        ArrayList<String> foundItems = new ArrayList<>();
        int i = 0;

        try (Scanner scanner =
                     new Scanner(getResources().openRawResource(R.raw.products))) {
            String line;
            while ((line = scanner.nextLine()) != null) {
                String[] values = line.split(",");
                if (values.length > 0 && values[i].equals(searchValue)) {
                    // Fetch the items on the found row
                    foundItems.addAll(Arrays.asList(values));
                }

                i++;
            }
        }

        return foundItems;
    }
}