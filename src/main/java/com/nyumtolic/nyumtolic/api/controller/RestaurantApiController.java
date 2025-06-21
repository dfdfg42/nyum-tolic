package com.nyumtolic.nyumtolic.api.controller;


import com.nyumtolic.nyumtolic.api.domain.PageResponse;
import com.nyumtolic.nyumtolic.api.domain.RestaurantDTO;
import com.nyumtolic.nyumtolic.api.domain.ReviewLogDTO;
import com.nyumtolic.nyumtolic.domain.ReviewLog;
import com.nyumtolic.nyumtolic.security.dto.UserDTO;
import com.nyumtolic.nyumtolic.security.service.UserService;
import com.nyumtolic.nyumtolic.service.RestaurantService;
import com.nyumtolic.nyumtolic.service.ReviewLogService;
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
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
@Tag(name = "음식점", description = "냠톨릭 음식점 데이터와 관련된 API입니다.")
public class RestaurantApiController {
    //
    private final RestaurantService restaurantService;
    private final UserService userService;
    private final ReviewLogService reviewLogService;



    // ------------------- 만냠 비활성화로 주석 처리

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





    // --------------- 추천 로직 일시 비활성화로 주석 처리


    @GetMapping("/users")
    @Operation(summary = "GET all users", description = "모든 사용자 목록을 가져옵니다.")
    public List<UserDTO> getAllUsers() {
        return userService.getAllUserDTOs();
    }

    @GetMapping("/review-logs")
    public List<ReviewLogDTO> getAllReviewLogs() {
        return reviewLogService.getAllReviewLogs().stream()
                .map(ReviewLogDTO::new)
                .collect(Collectors.toList());
    }

    @GetMapping("/review-logs/user/{userId}")
    public List<ReviewLog> getReviewLogsByUser(@PathVariable Long userId) {
        return reviewLogService.getReviewLogsByUserId(userId);  // 특정 사용자의 리뷰 기록
    }
}