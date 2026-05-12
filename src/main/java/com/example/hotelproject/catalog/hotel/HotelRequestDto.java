package com.example.hotelproject.catalog.hotel;

import jakarta.validation.constraints.*;

public class HotelRequestDto {

    @NotBlank(message = "Hotel name must not be blank")
    @Size(min = 2, max = 100, message = "Hotel name must be between 2 and 100 characters")
    private String name;

    @NotBlank(message = "Description must not be blank")
    @Size(min = 10, max = 1000, message = "Description must be between 10 and 1000 characters")
    private String description;

    @NotBlank(message = "Address must not be blank")
    @Size(min = 5, max = 200, message = "Address must be between 5 and 200 characters")
    private String address;

    @NotBlank(message = "City must not be blank")
    @Size(min = 2, max = 50, message = "City must be between 2 and 50 characters")
    private String city;

    @NotBlank(message = "Country must not be blank")
    @Size(min = 2, max = 50, message = "Country must be between 2 and 50 characters")
    private String country;

    private String postalCode;

    @NotBlank(message = "Phone number must not be blank")
    @Pattern(regexp = "^[+]?[0-9]{10,15}$", message = "Invalid phone number format")
    private String phone;

    @Email(message = "Email must be valid")
    private String email;

    @NotNull(message = "Star rating is required")
    @Min(value = 1, message = "Star rating must be at least 1")
    @Max(value = 5, message = "Star rating must be at most 5")
    private Integer starRating;

    // Getters and Setters
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }

    public String getCountry() { return country; }
    public void setCountry(String country) { this.country = country; }

    public String getPostalCode() { return postalCode; }
    public void setPostalCode(String postalCode) { this.postalCode = postalCode; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public Integer getStarRating() { return starRating; }
    public void setStarRating(Integer starRating) { this.starRating = starRating; }
}