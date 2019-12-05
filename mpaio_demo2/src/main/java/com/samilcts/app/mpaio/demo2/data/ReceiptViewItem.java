package com.samilcts.app.mpaio.demo2.data;

import java.io.Serializable;

/**
 * Created by mskim on 2015-09-14.
 * mskim@31cts.com
 */
public class ReceiptViewItem implements Serializable {

    private final String name;

    public int getAmount() {
        return amount;
    }

    public String getName() {
        return name;
    }

    public double getPrice() {
        return price;
    }

    private final int amount;
    private final double price;

    public ReceiptViewItem(String name, int amount, double price) {

        this.name = name;
        this.amount = amount;
        this.price = price;

    }

}