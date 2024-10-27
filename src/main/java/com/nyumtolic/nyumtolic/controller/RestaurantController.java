package com.nyumtolic.nyumtolic.controller;


import com.nyumtolic.nyumtolic.s3.S3Service;
import com.nyumtolic.nyumtolic.domain.Category;
import com.nyumtolic.nyumtolic.domain.Restaurant;
import com.nyumtolic.nyumtolic.review.ReviewService;
import com.nyumtolic.nyumtolic.review.ReviewWithVotesDTO;
import com.nyumtolic.nyumtolic.service.CategoryService;
import com.nyumtolic.nyumtolic.service.RestaurantService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RequiredArgsConstructor
@RequestMapping("/restaurant")
@Controller
public class RestaurantController {

    private final RestaurantService restaurantService;
    private final ReviewService reviewService;
    private final CategoryService categoryService;
    private final S3Service s3Service;
    private static final Logger logger = LoggerFactory.getLogger(RestaurantController.class);


    @GetMapping(value = "/detail/{id}")
    public String detail(Model model, @PathVariable("id") Long id,
                         @PageableDefault(size = 6) Pageable pageable) {
        this.restaurantService.getRestaurantsById(id).ifPresent(restaurant -> model.addAttribute("restaurant", restaurant));
        Restaurant restaurant = restaurantService.getRestaurantsById(id).orElse(null);
        restaurant.getReviews().forEach(review ->
                logger.info("Review by {}: {}", review.getAuthor().getNickname(), review.getContent())
        );

        Optional<Restaurant> restaurantsById = restaurantService.getRestaurantsById(id);
        if (restaurantsById.isPresent()){

            //랜더링 수정
            String manuString = String.join(", ", restaurantsById.get().getMenu());
            model.addAttribute("menuString", manuString);
            List<String> catagoryNames= new ArrayList<>();
            for (Category category: restaurantsById.get().getCategories()){
                catagoryNames.add(category.getName());
            }
            String categoryString = String.join(", ", catagoryNames);
            model.addAttribute("categoryString", categoryString);

            Page<ReviewWithVotesDTO> reviewWithVotesPage = reviewService.findReviewsWithVotesByRestaurantId(id, pageable);
            model.addAttribute("reviewsPage", reviewWithVotesPage);

        }


        return "restaurant/detail";
    }

    @GetMapping("/list")
    public String showRestaurantList(@RequestParam(value = "categoryId", required = false) Long categoryId, Model model,
                                     @RequestParam(value = "sort", defaultValue = "id") String sort) {
        List<Restaurant> restaurants;
        if (categoryId != null) {

            if ("userRating".equals(sort)||"name".equals(sort)){
                restaurants = restaurantService.getAllByCategoryIdSorted(categoryId, sort);
            }
            else {
                // 특정 카테고리 ID가 제공된 경우, 해당 카테고리의 맛집 리스트를 가져옵니다.
                restaurants = restaurantService.findAllByCategoryId(categoryId);
            }
        }
        else {
            if ("userRating".equals(sort)||"name".equals(sort)){
                restaurants = restaurantService.getAllRestaurantsBySorted(sort);
            }
            else {
                // 카테고리 ID가 제공되지 않은 경우, 전체 맛집 리스트를 가져옵니다.
                restaurants = restaurantService.getAllRestaurants();
            }
        }
        model.addAttribute("restaurants", restaurants);
        model.addAttribute("categoryId",categoryId);
        return "restaurant/list"; // 맛집 리스트 페이지로 이동
    }

    @GetMapping("/recommendation")
    public String recommendRestaurant(@RequestParam(value = "excludedCategories", required = false) String excludedCategories, Model model) {
        model.addAttribute("excludedCategories", excludedCategories != null ? excludedCategories : "");
        if (excludedCategories != null) {
            String[] categoriesArray = excludedCategories.split("\\s*,\\s*");
            Optional<Restaurant> recommendedRestaurant = restaurantService.recommendRandomRestaurantExcludingCategories(categoriesArray);
            model.addAttribute("recommendedRestaurant", recommendedRestaurant.orElse(null));
        }
        return "restaurant/recommendation";
    }

    //관리자 페이지용

    // 관리자 페이지 전용
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @GetMapping("/admin/list")
    public String listRestaurantsForAdmin(Model model) {
        model.addAttribute("restaurants", restaurantService.findAll());
        return "restaurant/admin/restaurant_admin_list";
    }

    // 레스토랑 생성 폼 보여주기 (관리자용)
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @GetMapping("/admin/create")
    public String showCreateFormForAdmin(Model model) {
        Restaurant restaurant = new Restaurant();
        // categories가 이미 초기화되어 있는지 확인 (엔티티에서 초기화했다면 이 단계는 생략 가능)
        if (restaurant.getCategories() == null) {
            restaurant.setCategories(new ArrayList<>());
        }
        model.addAttribute("restaurant", restaurant);

        // allCategories 추가
        List<Category> allCategories = categoryService.findAll();
        model.addAttribute("allCategories", allCategories);

        return "restaurant/admin/restaurant_admin_form";
    }

    // 레스토랑 저장 또는 업데이트 (관리자용)
// 레스토랑 저장 또는 업데이트 (관리자용)
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PostMapping("/admin/save")
    public String saveRestaurantForAdmin(@ModelAttribute("restaurant") Restaurant restaurant,
                                            @RequestParam("photoFile")MultipartFile file) {
        Restaurant findedRestaurant = new Restaurant();
        if (restaurant.getId() != null) findedRestaurant = restaurantService.findById(restaurant.getId());

        findedRestaurant.setName(restaurant.getName());
        findedRestaurant.setAddress(restaurant.getAddress());
        findedRestaurant.setPhoneNumber(restaurant.getPhoneNumber());
        findedRestaurant.setRating(restaurant.getRating());
        findedRestaurant.setDescription(restaurant.getDescription());
        findedRestaurant.setTravelTime(restaurant.getTravelTime());
        findedRestaurant.setMenu(restaurant.getMenu());
        findedRestaurant.setCategories(restaurant.getCategories());
        findedRestaurant.setLatitude(restaurant.getLatitude());
        findedRestaurant.setLongitude(restaurant.getLongitude());
        findedRestaurant.setUserRating(restaurant.getUserRating());

        if (findedRestaurant.getPhoto() != null) s3Service.deleteFileByURL(findedRestaurant.getPhoto());
        try {
            String fileName = "restaurant/" + UUID.randomUUID().toString();
            String url = s3Service.uploadFileWithName(file, fileName);
            findedRestaurant.setPhoto(url);
        } catch (Exception e) {
            logger.error(e.getMessage());
        }

        restaurantService.save(findedRestaurant);

        return "redirect:/restaurant/admin/list";
    }


    // 레스토랑 수정 폼 (관리자용)
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @GetMapping("/admin/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model) {
        Restaurant restaurant = restaurantService.findById(id);
        List<Category> allCategories = categoryService.findAll();
        model.addAttribute("restaurant", restaurant);
        model.addAttribute("allCategories", allCategories);
        return "restaurant/admin/restaurant_admin_form";
    }

    // 레스토랑 삭제 (관리자용)
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @GetMapping("/admin/delete/{id}")
    public String deleteRestaurantForAdmin(@PathVariable Long id) {
        restaurantService.delete(id);
        return "redirect:/restaurant/admin/list";
    }


}
