package io.bdj.webshop.model;

/**
 * Represents a customer of the boutique
 */
public class Customer {

    private String lastname;
    private String firstname;
    private String email;
    private String street;
    private String city;
    private String zip;
    private String country;
    private String username;
    private String password;

    public String getLastname() {

        return lastname;
    }

    public void setLastname(final String lastname) {

        this.lastname = lastname;
    }

    public String getFirstname() {

        return firstname;
    }

    public void setFirstname(final String firstname) {

        this.firstname = firstname;
    }

    public String getEmail() {

        return email;
    }

    public void setEmail(final String email) {

        this.email = email;
    }

    public String getStreet() {

        return street;
    }

    public void setStreet(final String street) {

        this.street = street;
    }

    public String getCity() {

        return city;
    }

    public void setCity(final String city) {

        this.city = city;
    }

    public String getZip() {

        return zip;
    }

    public void setZip(final String zip) {

        this.zip = zip;
    }

    public String getCountry() {

        return country;
    }

    public void setCountry(final String country) {

        this.country = country;
    }

    public String getUsername() {

        return username;
    }

    public void setUsername(final String username) {

        this.username = username;
    }

    public String getPassword() {

        return password;
    }

    public void setPassword(final String password) {

        this.password = password;
    }

    @Override
    public String toString() {

        final StringBuilder sb = new StringBuilder("Customer{");
        sb.append("username='").append(username).append('\'');
        sb.append(", lastname='").append(lastname).append('\'');
        sb.append(", firstname='").append(firstname).append('\'');
        sb.append(", email='").append(email).append('\'');
        sb.append(", street='").append(street).append('\'');
        sb.append(", city='").append(city).append('\'');
        sb.append(", zip='").append(zip).append('\'');
        sb.append(", country='").append(country).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
