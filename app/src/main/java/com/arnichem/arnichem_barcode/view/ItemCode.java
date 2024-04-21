package com.arnichem.arnichem_barcode.view;

import java.io.Serializable;

public class ItemCode implements Serializable {

    private  String item_Code,barcode,weight,height,volume,unit,owner,max_pressure;


    public ItemCode(String item_Code, String barcode, String weight, String height, String volume, String unit, String owner, String max_pressure) {
        this.item_Code = item_Code;
        this.barcode = barcode;
        this.weight = weight;
        this.height = height;
        this.volume = volume;
        this.unit = unit;
        this.owner = owner;
        this.max_pressure = max_pressure;
    }







    public String getBarcode() {
        return barcode;
    }

    public void setBarcode(String barcode) {
        this.barcode = barcode;
    }

    public String getWeight() {
        return weight;
    }

    public void setWeight(String weight) {
        this.weight = weight;
    }

    public String getHeight() {
        return height;
    }

    public void setHeight(String height) {
        this.height = height;
    }

    public String getVolume() {
        return volume;
    }

    public void setVolume(String volume) {
        this.volume = volume;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public String getMax_pressure() {
        return max_pressure;
    }

    public void setMax_pressure(String max_pressure) {
        this.max_pressure = max_pressure;
    }

    public String getItem_Code() {
        return item_Code;
    }

    public void setItem_Code(String item_Code) {
        this.item_Code = item_Code;
    }

    public ItemCode() {
    }
}
