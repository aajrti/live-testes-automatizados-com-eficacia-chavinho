package br.com.unipds.csvreader.model;

import br.com.unipds.csvreader.core.CsvRecordReader;

import java.util.function.Consumer;

public class Product {

    private long index;
    private String name;
    private String description;
    private String brand;
    private String category;
    private double price;
    private String currency;
    private int stock;
    private String ean;
    private String color;
    private String size;
    private String availability;
    private String internalId;

    public Product() {
    }

    public long getIndex() {
        return index;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getBrand() {
        return brand;
    }

    public String getCategory() {
        return category;
    }

    public double getPrice() {
        return price;
    }

    public String getCurrency() {
        return currency;
    }

    public int getStock() {
        return stock;
    }

    public String getEan() {
        return ean;
    }

    public String getColor() {
        return color;
    }

    public String getSize() {
        return size;
    }

    public String getAvailability() {
        return availability;
    }

    public String getInternalId() {
        return internalId;
    }

    public static void processarCsv(String caminhoArquivo, Consumer<Product> processador) {
        CsvRecordReader.builder()
                .withHeader(true)
                .build()
                .process(caminhoArquivo, Product.class, processador);
    }
}