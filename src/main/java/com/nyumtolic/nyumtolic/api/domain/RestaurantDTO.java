package com.nyumtolic.nyumtolic.api.domain;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
public class RestaurantDTO {
    private Long id;
    private String photo;
    private String name;
    private String address;
    private String phoneNumber;
    private List<CategoryDTO> categories;
    private Double rating;
    private List<String> menu;
    private String description;
    private Integer travelTime;
    private Double latitude;
    private Double longitude;
    private Double userRating;
}
