package com.samilcts.app.mpaio.demo2.data;

import java.io.Serializable;

/**
 * Created by mskim on 2015-09-14.
 * mskim@31cts.com
 */
public class Product implements Serializable {

    private int productNum;
    private String name;
    private String img;
    private double price;
    private String barcode;

    public String getName() {
        return name;
    }
    public String getBarcode() {
        return barcode;
    }
    public String getImg() {
        return img;
    }
    public double getPrice() {
        return price;
    }

    public void setProductNum(int productNum) {
        this.productNum = productNum;
    }
    public void setProductName(String productName) {
        this.name = productName;
    }
    public void setPrice(int price) {
        this.price = price;
    }
    public void setImg(String img) {
        this.img = img;
    }
    public void setBarcode(String barcode) {
        this.barcode = barcode;
    }

    public Product(String name, String img, double price, String barcode) {

        this.name = name;
        this.img = img;
        this.price = price;
        this.barcode = barcode;
    }

    public Product(){}
}