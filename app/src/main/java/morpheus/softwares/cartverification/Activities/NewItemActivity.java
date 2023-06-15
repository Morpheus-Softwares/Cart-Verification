package morpheus.softwares.cartverification.Activities;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

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

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import morpheus.softwares.cartverification.Models.Database;
import morpheus.softwares.cartverification.Models.Links;
import morpheus.softwares.cartverification.Models.Products;
import morpheus.softwares.cartverification.R;

public class NewItemActivity extends AppCompatActivity {
    private final String DATABASEURL = new Links().getDATABASEURL();
    Database database;
    Products products;

    TextView idNum, date;

    EditText serialNumber, productName, owner, price;

    FloatingActionButton fab;

    private void copyRawResourceToFile(String fileName) throws IOException {
        InputStream inputStream = getResources().openRawResource(R.raw.products);
        // "your_raw_resource" with the actual resource name
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        FileOutputStream outputStream = openFileOutput(fileName, Context.MODE_PRIVATE);
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(outputStream));

        String line;
        while ((line = reader.readLine()) != null) {
            writer.write(line);
            writer.newLine();
        }

        writer.close();
        reader.close();
        inputStream.close();
        outputStream.close();
    }

    private void appendItemsToFile(String fileName, String[] newItems) throws IOException {
        FileOutputStream outputStream = openFileOutput(fileName, Context.MODE_APPEND);
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(outputStream));

        for (String item : newItems) {
            writer.write(item);
            writer.newLine();
        }

        writer.close();
        outputStream.close();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_item);

        idNum = findViewById(R.id.newSerialNumber);
        serialNumber = findViewById(R.id.verifyDeviceSN);
        productName = findViewById(R.id.verifyProductName);
        owner = findViewById(R.id.verifyOwner);
        price = findViewById(R.id.price);
        date = findViewById(R.id.date);

        database = new Database(NewItemActivity.this);

        idNum.setText(String.valueOf(getIntent().getStringExtra("productID")));

        fab = findViewById(R.id.newSync);
        fab.setOnClickListener(view -> insertProducts());
    }

    private void insertProducts() {
        final ProgressDialog dialog = ProgressDialog.show(this, "Cloud sync", "Synchronizing records to cloud, please wait...");

        String sn = String.valueOf(serialNumber.getText()).trim();
        String name = String.valueOf(productName.getText()).trim();
        int id = Integer.parseInt(String.valueOf(idNum.getText()).trim());
        String deviceOwner = String.valueOf(owner.getText()).trim();
        double productPrice = Double.parseDouble(String.valueOf(price.getText()).trim());
        Date today = new Date();
        @SuppressLint("SimpleDateFormat")
        SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy");
        String date = format.format(today);

        if (String.valueOf(id).trim().isEmpty()) {
            idNum.setError("Provide product's ID");
            dialog.dismiss();
        } else if (sn.isEmpty()) {
            serialNumber.setError("Provide serial number");
            dialog.dismiss();
        } else if (name.isEmpty()) {
            productName.setError("Provide product's name");
            dialog.dismiss();
        } else if (deviceOwner.isEmpty()) {
            owner.setError("Provide owner's name");
            dialog.dismiss();
        } else if (String.valueOf(productPrice).trim().isEmpty()) {
            price.setError("Provide product's price");
            dialog.dismiss();
        } else {
            dialog.dismiss();
            String[] newItems = {String.valueOf(id), name, sn, deviceOwner,
                    String.valueOf(productPrice), date};
            try {
                // Copy the file from raw resources to internal storage
                copyRawResourceToFile("products.txt");

                // Append items to the file
                appendItemsToFile("products.txt", newItems);

                Toast.makeText(getApplicationContext(), "Product added to the file " +
                                "successfully.",
                        Toast.LENGTH_SHORT).show();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Offline mode
     */
    private void insertProduct() {
        final ProgressDialog dialog = ProgressDialog.show(this, "Cloud sync", "Synchronizing records to cloud, please wait...");

        String sn = String.valueOf(serialNumber.getText()).trim();
        String name = String.valueOf(productName.getText()).trim();
        int id = Integer.parseInt(String.valueOf(idNum.getText()).trim());
        String deviceOwner = String.valueOf(owner.getText()).trim();
        double productPrice = Double.parseDouble(String.valueOf(price.getText()).trim());
        Date today = new Date();
        @SuppressLint("SimpleDateFormat")
        SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy");
        String date = format.format(today);

        if (String.valueOf(id).trim().isEmpty()) {
            idNum.setError("Provide product's ID");
            dialog.dismiss();
        } else if (sn.isEmpty()) {
            serialNumber.setError("Provide serial number");
            dialog.dismiss();
        } else if (name.isEmpty()) {
            productName.setError("Provide product's name");
            dialog.dismiss();
        } else if (deviceOwner.isEmpty()) {
            owner.setError("Provide owner's name");
            dialog.dismiss();
        } else if (String.valueOf(productPrice).trim().isEmpty()) {
            price.setError("Provide product's price");
            dialog.dismiss();
        } else {
            products = new Products(id, sn, name, deviceOwner, productPrice, date);
            database.insertProduct(products);
            dialog.dismiss();
        }
    }

    /**
     * Online mode
     */
    private void sync() {
        final ProgressDialog dialog = ProgressDialog.show(this, "Cloud sync", "Synchronizing records to cloud, please wait...");

        String sn = String.valueOf(serialNumber.getText()).trim();
        String name = String.valueOf(productName.getText()).trim();
        String id = String.valueOf(idNum.getText()).trim();
        String deviceOwner = String.valueOf(owner.getText()).trim();

        if (id.isEmpty()) {
            idNum.setError("Provide product's ID");
            dialog.dismiss();
        } else if (sn.isEmpty()) {
            serialNumber.setError("Provide serial number");
            dialog.dismiss();
        } else if (name.isEmpty()) {
            productName.setError("Provide product's name");
            dialog.dismiss();
        } else if (deviceOwner.isEmpty()) {
            owner.setError("Provide owner's name");
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
                    params.put("ID", name.substring(0, 3) + id);
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