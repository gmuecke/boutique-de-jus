package io.bdj.webshop.model;

import java.util.List;

/**
 *
 */
public class Product {

    private int id;
    private String name;
    private String description;
    private String category;
    private List<String> tags;
    private byte[] image;
    private Double price;

    public int getId() {

        return id;
    }

    public void setId(final int id) {

        this.id = id;
    }

    public String getName() {

        return name;
    }

    public void setName(final String name) {

        this.name = name;
    }

    public String getDescription() {

        return description;
    }

    public void setDescription(final String description) {

        this.description = description;
    }

    public String getCategory() {

        return category;
    }

    public void setCategory(final String category) {

        this.category = category;
    }

    public List<String> getTags() {

        return tags;
    }

    public void setTags(final List<String> tags) {

        this.tags = tags;
    }

    public byte[] getImage() {

        return image;
    }

    public void setImage(final byte[] image) {

        this.image = image;
    }

    public Double getPrice() {

        return price;
    }

    public void setPrice(final Double price) {

        this.price = price;
    }
}
