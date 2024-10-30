package com.nyumtolic.nyumtolic.api.controller;


import com.nyumtolic.nyumtolic.api.domain.PageResponse;
import com.nyumtolic.nyumtolic.api.domain.RestaurantDTO;
import com.nyumtolic.nyumtolic.service.RestaurantService;
import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
@Tag(name = "음식점", description = "냠톨릭 음식점 데이터와 관련된 API입니다.")
public class RestaurantApiController {

    private final RestaurantService restaurantService;

    @GetMapping("/restaurants")
    @Operation(summary = "GET restaurant", description = "음식점 목록을 가져옵니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "성공", useReturnTypeSchema = true)
    })
    @Parameters({
            @Parameter(name = "page", description = "페이지 번호", example = "1"),
            @Parameter(name = "size", description = "페이지당 음식점 개수", example = "10")
    })
    public PageResponse<RestaurantDTO> getCreateAPI(@Parameter(hidden = true) @PageableDefault(size = 5) Pageable pageable) {
        return restaurantService.getAllRestaurantsDTO(pageable);
    }


    @GetMapping("/restaurants/all")
    @Operation(summary = "GET all restaurants", description = "모든 음식점 목록을 가져옵니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "성공", content = @Content(mediaType = "application/json", schema = @Schema(implementation = List.class)))
    })
    public List<RestaurantDTO> getAllRestaurants() {
        // findAll 메서드를 활용하여 전체 음식점 목록을 DTO로 반환
        return restaurantService.findAll().stream()
                .map(restaurantService::createRestaurantDTO)
                .collect(Collectors.toList());
    }
}