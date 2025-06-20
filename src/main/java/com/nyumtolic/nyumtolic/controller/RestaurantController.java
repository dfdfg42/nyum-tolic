package com.nyumtolic.nyumtolic.controller;


import com.nyumtolic.nyumtolic.cloudinary.CloudinaryImageService;
import com.nyumtolic.nyumtolic.post.notice.NoticePost;
import com.nyumtolic.nyumtolic.post.notice.NoticePostService;
import com.nyumtolic.nyumtolic.s3.S3Service;
import com.nyumtolic.nyumtolic.domain.Category;
import com.nyumtolic.nyumtolic.domain.Restaurant;
import com.nyumtolic.nyumtolic.review.ReviewService;
import com.nyumtolic.nyumtolic.review.ReviewWithVotesDTO;
import com.nyumtolic.nyumtolic.security.oauth.PrincipalDetails;
import com.nyumtolic.nyumtolic.service.CategoryService;
import com.nyumtolic.nyumtolic.service.RecommendationService;
import com.nyumtolic.nyumtolic.service.RestaurantService;
import com.nyumtolic.nyumtolic.service.VisitLogService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.security.Principal;
import java.util.*;

@RequiredArgsConstructor
@RequestMapping("/restaurant")
@Controller
public class RestaurantController {

    private final VisitLogService visitLogService;
    private final RestaurantService restaurantService;
    private final ReviewService reviewService;
    private final CategoryService categoryService;
    private final S3Service s3Service;
    private final RecommendationService recommendationService;
    private final NoticePostService noticePostService;
    private final CloudinaryImageService cloudinaryImageService;
    private static final Logger logger = LoggerFactory.getLogger(RestaurantController.class);


    @GetMapping(value = "/detail/{id}")
    public String detail(Model model, @PathVariable("id") Long id,
                         @PageableDefault(size = 6) Pageable pageable,
                         @AuthenticationPrincipal PrincipalDetails principalDetails) {
        //visitlogs는 필요없어서 비활성화
/*        // 로그인된 사용자가 있는 경우 userId 가져오기
        if (principalDetails != null) {
            Long userId = principalDetails.getSiteUser().getId();  // SiteUser의 PK ID
            visitLogService.logVisit(userId, id);  // 방문 로그 기록
        }*/


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
                                     @RequestParam(value = "sort", defaultValue = "name") String sort) {
        List<Restaurant> restaurants;
        if (categoryId != null) {
            if ("userRating".equals(sort) || "name".equals(sort)) {
                restaurants = restaurantService.getAllByCategoryIdSorted(categoryId, sort);
            } else {
                // 특정 카테고리 ID가 제공된 경우, 해당 카테고리의 맛집 리스트를 가져옵니다.
                restaurants = restaurantService.findAllByCategoryId(categoryId);
            }
        } else {
            if ("userRating".equals(sort) || "name".equals(sort)) {
                restaurants = restaurantService.getAllRestaurantsBySorted(sort);
            } else {
                // 카테고리 ID가 제공되지 않은 경우, 전체 맛집 리스트를 가져옵니다.
                restaurants = restaurantService.getAllRestaurants();
            }
        }
        model.addAttribute("restaurants", restaurants);
        model.addAttribute("categoryId", categoryId);


        List<NoticePost> pinnedNotices = noticePostService.getPinnedNotices();
        model.addAttribute("pinnedNotices", pinnedNotices);

        return "restaurant/list"; // 맛집 리스트 페이지로 이동
    }

    /**
     * 음식점 추천 페이지
     *
     * 1) excludedCategories 파라미터를 받으면 세션에 갱신
     * 2) reset=true 일 때 추천 기록(history)와 제외 목록(excludedIds) 초기화
     * 3) history가 비어있으면 새로 추천, 그렇지 않으면 최근 추천 값을 유지
     */
    @GetMapping("/recommendation")
    public String showRecommendation(
            @RequestParam(value = "excludedCategories", required = false) String excludedCategories,
            @RequestParam(value = "reset", required = false) Boolean reset,
            HttpSession session,
            Model model
    ) {
        // 1) 세션에서 history와 excludedIds 가져오기 (없으면 생성)
        Deque<Long> history = getOrCreateHistory(session);
        Set<Long> excludedIds = getOrCreateExcludedIds(session);

        // 2) 카테고리 문자열 세션에 저장 (파라미터가 있다면 갱신)
        if (excludedCategories != null && !excludedCategories.isBlank()) {
            session.setAttribute("sessionExcludedCats", excludedCategories.trim());
        }
        String sessionCats = (String) session.getAttribute("sessionExcludedCats");
        if (sessionCats == null) {
            sessionCats = "";  // 세션에 없으면 빈 문자열
        }

        // 3) reset 파라미터가 true라면 기록 초기화
        if (Boolean.TRUE.equals(reset)) {
            history.clear();
            excludedIds.clear();
            model.addAttribute("wasReset", true);
        } else {
            model.addAttribute("wasReset", false);
        }

        // 4) 만약 history가 비어있다면 새로운 추천 시도
        if (history.isEmpty()) {
            recommendRandom(sessionCats, excludedIds, history, model);
        } else {
            // 이미 추천된 내역이 있으면 최근 추천 항목을 모델에 세팅
            Long recentId = history.peek();
            model.addAttribute("recommendedRestaurant", restaurantService.findById(recentId));
        }

        // 5) 뷰에 넘길 정보들
        model.addAttribute("excludedCategories", sessionCats);
        model.addAttribute("excludedCount", excludedIds.size());

        return "restaurant/recommendation";
    }

    /**
     * 다음 추천 (현재 추천을 제외 목록에 추가하고 새로운 추천을 하나 뽑아온 뒤 리다이렉트)
     */
    @PostMapping("/recommendation/next")
    public String nextRecommendation(
            @RequestParam(value = "excludedCategories", required = false) String excludedCategories,
            HttpSession session
    ) {
        // 1) 세션에서 history, excludedIds 가져오기
        Deque<Long> history = getOrCreateHistory(session);
        Set<Long> excludedIds = getOrCreateExcludedIds(session);

        // 2) 세션의 카테고리 문자열 갱신
        if (excludedCategories != null) {
            session.setAttribute("sessionExcludedCats", excludedCategories.trim());
        }
        String sessionCats = (String) session.getAttribute("sessionExcludedCats");
        if (sessionCats == null) {
            sessionCats = "";
        }

        // 3) 현재 추천된 항목이 있다면 제외 목록에 등록
        if (!history.isEmpty()) {
            excludedIds.add(history.pop());
        }

        // 4) 새로 추천 시도
        recommendRandom(sessionCats, excludedIds, history, null);

        // 5) 리다이렉트
        return "redirect:/restaurant/recommendation";
    }

    /**
     * 세션에 history(스택) 정보가 있으면 가져오고, 없으면 새로 생성
     */
    @SuppressWarnings("unchecked")
    private Deque<Long> getOrCreateHistory(HttpSession session) {
        Deque<Long> history = (Deque<Long>) session.getAttribute("history");
        if (history == null) {
            history = new ArrayDeque<>();
            session.setAttribute("history", history);
        }
        return history;
    }

    /**
     * 세션에 제외 목록이 있으면 가져오고, 없으면 새로 생성
     */
    @SuppressWarnings("unchecked")
    private Set<Long> getOrCreateExcludedIds(HttpSession session) {
        Set<Long> excludedIds = (Set<Long>) session.getAttribute("excludedIds");
        if (excludedIds == null) {
            excludedIds = new HashSet<>();
            session.setAttribute("excludedIds", excludedIds);
        }
        return excludedIds;
    }

    /**
     * 랜덤 추천 도우미
     *  - 후보가 있으면 history에 push & model에 세팅
     *  - 후보가 없으면 model.noCandidates = true
     */
    private void recommendRandom(String sessionCats, Set<Long> excludedIds, Deque<Long> history, Model model) {
        String[] cats = sessionCats.isEmpty() ? new String[0] : sessionCats.split("\\s*,\\s*");
        Optional<Restaurant> optional = restaurantService.recommendRandomRestaurantExcluding(cats, excludedIds);
        if (optional.isPresent()) {
            Restaurant r = optional.get();
            history.push(r.getId());
            if (model != null) {
                model.addAttribute("recommendedRestaurant", r);
            }
        } else {
            if (model != null) {
                model.addAttribute("noCandidates", true);
            }
        }
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



    // 어드민 권한이 있는 사용자만 접근 가능하도록 설정
    /*@PreAuthorize("hasRole('ROLE_ADMIN')")
    @PostMapping("/admin/update-recommendations")
    public String updateRecommendations() {
        recommendationService.fetchRecommendations();
        return "redirect:/restaurant/admin/list";  // 어드민 페이지로 리다이렉트 또는 원하는 페이지로 이동
    }*/
}