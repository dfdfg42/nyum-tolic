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
    @Schema(description =  "식당 식별자", example = "11")
    private Long id;
    @Schema(description =  "식당 이미지", example = "images/나의 유부.webp")
    private String photo;
    @Schema(description =  "식당 이름", example = "나의 유부")
    private String name;
    @Schema(description =  "식당 주소", example = "경기 부천시 원미구 지봉로34번길 25")
    private String address;
    @Schema(description =  "전화번호", example = "032-123-1234")
    private String phoneNumber;
    @Schema(description =  "음식점 카테고리")
    private List<CategoryDTO> categories;
    @Schema(description =  "별점", example = "3")
    private Double rating;
    @Schema(description =  "메뉴", example = "[\"유부초밥\"]")
    private List<String> menu;
    @Schema(description =  "설명", example = "유부초밥 대박박")
    private String description;
    @Schema(description =  "가톨릭대학교 정문부터의 거리", example = "2")
    private Integer travelTime;
    @Schema(description =  "위도", example = "37.4864432134")
    private Double latitude;
    @Schema(description =  "경도", example = "126.31289213")
    private Double longitude;
    @Schema(description =  "사용자 별점", example = "3")
    private Double userRating;
}
