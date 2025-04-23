package org.Lskar.ProductManagement.domain;

public class Item {

    private int id;
    private int quantity;

    private double price;

    public Item(int id, int quantity) {
        this.id = id;
        this.quantity = quantity;
    }


    public Item(int id, int quantity, double price) {
        this.id = id;
        this.quantity = quantity;
        this.price = price;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    @Override
    public String toString() {
        return "Item{" +
                "id=" + id +
                ", quantity=" + quantity +
                '}';
    }

    public Double getPrice() {
        return price;
    }
}
