package com.arnichem.arnichem_barcode.PaymentReceipt;

import java.util.List;

public class GasTypeResponse {
    private Data data;

    public Data getData() {
        return data;
    }

    public static class Data {
        private List<String> gas_types;

        public List<String> getGasTypes() {
            return gas_types;
        }
    }
}
