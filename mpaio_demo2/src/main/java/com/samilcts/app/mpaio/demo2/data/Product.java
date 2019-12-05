package com.samilcts.app.mpaio.demo2.data;

import java.io.Serializable;

/**
 * Created by mskim on 2015-09-14.
 * mskim@31cts.com
 */
public class Product implements Serializable {


    public String getName() {
        return name;
    }

    private String name;

    private int imageRes;
    private double price;

    public String getBarcode() {
        return barcode;
    }

    private String barcode;

    public int getImageRes() {
        return imageRes;
    }

    public double getPrice() {
        return price;
    }

    public Product(String name, int imageRes, double price, String barcode) {

        this.name = name;
        this.imageRes = imageRes;
        this.price = price;
        this.barcode = barcode;
    }

}