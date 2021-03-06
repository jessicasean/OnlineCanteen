package com.example.asus.onlinecanteen.model;

import java.io.Serializable;

/**
 * Created by ASUS on 3/2/2018.
 */

public class Cart implements Serializable {

    //Set Variables
    private String itemName;
    private int quantity;
    private int price;

    public Cart(String itemName, int price, int quantity) {
        this.itemName = itemName;
        this.price = price;
        this.quantity = quantity;
    }

    public String getProductName() {
        return itemName;
    }

    public int getProductPrice() { return price;}

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {this.quantity = quantity;}

    public void increaseQuantity(int incQuantity) {
        if(incQuantity < 0) return;
        this.quantity += incQuantity;
    }

    public void decreaseQuantity(int decQuantity) {
        if(decQuantity < 0) return;
        this.quantity -= decQuantity;
    }

    public static int getTotalPrice(Cart cart) {
        return cart.getQuantity() * cart.getProductPrice();
    }
}
