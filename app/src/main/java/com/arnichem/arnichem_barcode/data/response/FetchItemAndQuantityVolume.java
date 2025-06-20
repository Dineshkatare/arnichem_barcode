package com.arnichem.arnichem_barcode.data.response;


import java.util.List;

public class FetchItemAndQuantityVolume {
    private int dcno;
    private List<Item> items;
    private String message;

    // Getters and setters
    public int getDcno() {
        return dcno;
    }

    public void setDcno(int dcno) {
        this.dcno = dcno;
    }

    public List<Item> getItems() {
        return items;
    }

    public void setItems(List<Item> items) {
        this.items = items;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public static class Item {
        private String item;
        private float quantity_volume;

        // Getters and setters
        public String getItem() {
            return item;
        }

        public void setItem(String item) {
            this.item = item;
        }

        public float getQuantity_volume() {
            return quantity_volume;
        }

        public void setQuantity_volume(float quantity_volume) {
            this.quantity_volume = quantity_volume;
        }
    }
}
