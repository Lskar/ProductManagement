package org.Lskar.ProductManagement.domain;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.List;

public class Order {



    private int orderID;
    private String orderDate;
    private double totalPrice;
    private List<Item> items;

    public Order(List<Item> items) {
        double sum=0;
        for (Item item : items) {
            sum+=item.getPrice()*item.getQuantity();
        }
        this.totalPrice=sum;
        this.items = items;
    }

    public Order(int orderID, Timestamp orderDate, double totalPrice) {
        this.orderID = orderID;
        this.orderDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(orderDate);
        this.totalPrice = totalPrice;
    }

    public Order(int orderID, Timestamp orderDate, double totalPrice, List<Item> items) {
        this.orderID = orderID;
        this.orderDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(orderDate);
        this.totalPrice = totalPrice;
        this.items = items;
    }



    public int getOrderID() {
        return orderID;
    }

    public void setOrderID(int orderID) {
        this.orderID = orderID;
    }

    public String getOrderDate() {
        return orderDate;
    }

    public void setOrderDate(Timestamp orderDate) {
        this.orderDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(orderDate);
    }

    public double getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(double totalPrice) {
        this.totalPrice = totalPrice;
    }

    public List<Item> getItems() {
        return items;
    }

    public void setItems(List<Item> items) {
        this.items = items;
    }

    @Override
    public String toString() {
        return "Order{" +
                "orderID=" + orderID +
                ", orderDate=" + orderDate +
                ", totalPrice=" + totalPrice +
                ", items=" + items +
                '}';
    }

}
