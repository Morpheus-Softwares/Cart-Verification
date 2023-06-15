package morpheus.softwares.cartverification.Models;

public class Products {
    private int id;
    private double price;
    private String serialNumber, productName, owner, date;

    public Products(int id, String serialNumber, String productName, String owner,
                    double price, String date) {
        this.id = id;
        this.serialNumber = serialNumber;
        this.productName = productName;
        this.owner = owner;
        this.price = price;
        this.date = date;
    }

    public String getSerialNumber() {
        return serialNumber;
    }

    public void setSerialNumber(String serialNumber) {
        this.serialNumber = serialNumber;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    @Override
    public String toString() {
        return String.format("%d%s%s%s%s%s", id, serialNumber, productName, owner, price, date);
    }
}