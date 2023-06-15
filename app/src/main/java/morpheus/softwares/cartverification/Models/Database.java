package morpheus.softwares.cartverification.Models;

import android.annotation.SuppressLint;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

import java.util.ArrayList;

public class Database extends SQLiteOpenHelper {
    public static final String DATABASE_NAME = "DB.db", TABLE_PRODUCTS = "products",
            SN_COLUMN_NAME = "sn", PRODUCT_COLUMN_NAME = "product", OWNER_COLUMN_NAME = "owner",
            PRICE_COLUMN_NAME = "price", DATE_COLUMN_NAME = "date";
    public static final int DATABSE_VERSION = 1;

    public Database(@Nullable Context context) {
        super(context, DATABASE_NAME, null, DATABSE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + TABLE_PRODUCTS + " (id integer PRIMARY KEY AUTOINCREMENT, "
                + SN_COLUMN_NAME + " text, " + PRODUCT_COLUMN_NAME + " text, " + OWNER_COLUMN_NAME
                + " text, " + PRICE_COLUMN_NAME
                + " text, " + DATE_COLUMN_NAME + " text)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_PRODUCTS);
        onCreate(db);
    }

    /**
     * Adds a row to Products Table
     */
    public void insertProduct(Products products) {
        SQLiteDatabase db = this.getWritableDatabase();
        String sqlInsert = "INSERT INTO " + TABLE_PRODUCTS;
        sqlInsert += " values( null, '" + products.getProductName() + "', '" + products.getSerialNumber()
                + "', '" + products.getOwner() + "', '" + products.getPrice() + "', '" + products.getDate() + "' )";

        db.execSQL(sqlInsert);
        db.close();
    }

    /**
     * Selects and returns all the rows in Products Table
     */
    public ArrayList<Products> selectAllProducts() {
        String sqlQuery = "SELECT * FROM " + TABLE_PRODUCTS;

        SQLiteDatabase db = this.getWritableDatabase();
        @SuppressLint("Recycle")
        Cursor cursor = db.rawQuery(sqlQuery, null);


        ArrayList<Products> products = new ArrayList<>();
        while (cursor.moveToNext()) {
            Products currentProducts = new Products(cursor.getInt(0),
                    cursor.getString(1), cursor.getString(2),
                    cursor.getString(3), cursor.getDouble(4), cursor.getString(5));
            products.add(currentProducts);
        }

        db.close();
        return products;
    }

    /**
     * Removes the row with the selected word from Products Table
     */
    public void deleteProduct(String product) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_PRODUCTS, SN_COLUMN_NAME + " = ?", new String[]{product});
        db.close();
    }

    /**
     * Returns the total number of rows in Products Table
     */
    public int getProductsCount() {
        String countQuery = "SELECT * FROM " + TABLE_PRODUCTS;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(countQuery, null);
        cursor.close();
        return cursor.getCount();
    }

    /**
     * Clears all rows in Products Table and returns the number of rows remaining
     */
    public Integer clearProducts() {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.delete(TABLE_PRODUCTS, null, null);
    }
}