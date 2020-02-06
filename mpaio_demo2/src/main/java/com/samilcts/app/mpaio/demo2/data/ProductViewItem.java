package com.samilcts.app.mpaio.demo2.data;

import java.io.Serializable;

/**
 * Created by mskim on 2015-12-21.
 * mskim@31cts.com
 */
public class ProductViewItem implements Serializable {


    private Product product;

    public int getAmount() {
        return amount;
    }

    public Product getProduct() {
        return product;
    }

    private int amount;


    public void setAmount(int amount) {
        this.amount = amount;
    }

    public ProductViewItem(Product product, int amount) {

        this.product = product;
        this.amount = amount;

    }
}
