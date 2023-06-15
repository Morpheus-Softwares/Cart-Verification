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
import java.util.Scanner;

import morpheus.softwares.cartverification.Models.Database;
import morpheus.softwares.cartverification.Models.Links;
import morpheus.softwares.cartverification.Models.Products;
import morpheus.softwares.cartverification.R;

public class NewScanCodeActivity extends AppCompatActivity {
    private final String JSONURL = new Links().getIDSJSONURL();
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
        setContentView(R.layout.activity_new_scan_code);

        setUpPermission();

        check = findViewById(R.id.proceed);
        scannedCode = findViewById(R.id.code);
        scanView = findViewById(R.id.scannerView);
        fab = findViewById(R.id.scanEdit);

        database = new Database(NewScanCodeActivity.this);

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
            String idNum = String.valueOf(scannedCode.getText()).trim();
//            checkID(idNum);
//            checkIDs(idNum);
            checkIDNum(idNum);
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

    /**
     * Offline mode with text
     */
    private void checkIDNum(String idNum) {
        ProgressDialog dialog = ProgressDialog.show(this, "Cloud fetch", "Fetching device serial number from cloud, please wait...");

        idNum = String.valueOf(scannedCode.getText()).trim();

        if (isInDatabase(idNum)) {
            dialog.dismiss();

            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setCancelable(false);
            builder.setTitle("Update");
            builder.setIcon(R.drawable.ic_baseline_question_mark_24);
            builder.setInverseBackgroundForced(true);
            String finalIdNum = idNum;
            builder.setMessage(idNum + " already exists in cloud database...\nAre you trying to " +
                    "update device?").setPositiveButton("Yes", (alert, which) -> {
                startActivity(new Intent(NewScanCodeActivity.this, NewItemActivity.class).putExtra("productID", finalIdNum));
                alert.dismiss();
            }).setNegativeButton("No", (alert, which) -> alert.cancel());
            AlertDialog alertDialog = builder.create();
            alertDialog.show();
        } else {
            Intent intent = new Intent(NewScanCodeActivity.this, NewItemActivity.class);
            intent.putExtra("productID", idNum);
            startActivity(intent);
            dialog.dismiss();
            finish();
        }
    }


    /**
     * Offline mode
     */
    private void checkID(String idNum) {
        ProgressDialog dialog = ProgressDialog.show(this, "Cloud fetch", "Fetching device serial number from cloud, please wait...");

        idNum = String.valueOf(scannedCode.getText()).trim();

        ArrayList<Products> products = database.selectAllProducts();

        for (Products product : products) {
            if (product.getId() == Integer.parseInt(idNum)) {
                dialog.dismiss();

                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setCancelable(false);
                builder.setTitle("Update");
                builder.setIcon(R.drawable.ic_baseline_question_mark_24);
                builder.setInverseBackgroundForced(true);
                String finalIdNum = idNum;
                builder.setMessage(idNum + " already exists in cloud database...\nAre you trying to " +
                        "update device?").setPositiveButton("Yes", (alert, which) -> {
                    startActivity(new Intent(NewScanCodeActivity.this, NewItemActivity.class).putExtra("productID", finalIdNum));
                    alert.dismiss();
                }).setNegativeButton("No", (alert, which) -> alert.cancel());
                AlertDialog alertDialog = builder.create();
                alertDialog.show();
            } else {
                Intent intent = new Intent(NewScanCodeActivity.this, NewItemActivity.class);
                intent.putExtra("productID", idNum);
                startActivity(intent);
                dialog.dismiss();
                finish();
            }
        }
    }

    /**
     * Online mode
     */
    private void checkIDs(String IDNumber) {
        ProgressDialog dialog = ProgressDialog.show(this, "Cloud check", "Checking device serial number from cloud, please wait...");
        List<String> ids = new ArrayList<>();

        StringRequest request = new StringRequest(Request.Method.GET, JSONURL, response -> {

            try {
                JSONObject jsonObject = new JSONObject(response);
                JSONArray jsonArray = jsonObject.getJSONArray("items");

                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject object = jsonArray.getJSONObject(i);
                    String id = object.getString("ID");
                    ids.add(id);
                }
            } catch (JSONException e) {
                Snackbar.make(findViewById(R.id.scanner), "Encountered error while scanning " + IDNumber + " from cloud database, try again...", Snackbar.LENGTH_LONG).show();
                dialog.dismiss();
                e.printStackTrace();
            }

            if (ids.contains(IDNumber)) {
                dialog.dismiss();

                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setCancelable(false);
                builder.setTitle("Update?");
                builder.setIcon(R.drawable.ic_baseline_question_mark_24);
                builder.setInverseBackgroundForced(true);
                builder.setMessage(IDNumber + " already exists in cloud database...\nAre you " +
                        "trying to profile a new product?").setPositiveButton("Yes",
                        (alert, which) -> {
                            startActivity(new Intent(NewScanCodeActivity.this, NewItemActivity.class));
                            alert.dismiss();
                        }).setNegativeButton("No", (alert, which) -> alert.cancel());
                AlertDialog alertDialog = builder.create();
                alertDialog.show();
            } else {
                Intent intent = new Intent(NewScanCodeActivity.this, NewItemActivity.class);
                intent.putExtra("productID", IDNumber);
                startActivity(intent);
                dialog.dismiss();
                finish();
            }
        }, error -> {
            Snackbar.make(findViewById(R.id.scanner), "Encountered error while checking for " + IDNumber + " in cloud database, try again...", Snackbar.LENGTH_LONG).show();
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
                if (values.length > 0 && values[i].equals(searchValue)) {
                    return true;
                }
                i++;
            }
        }

        return false;
    }
}