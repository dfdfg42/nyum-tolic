//package com.nyumtolic.nyumtolic.config;
//
//import com.nyumtolic.nyumtolic.domain.Category;
//import com.nyumtolic.nyumtolic.domain.Restaurant;
//import com.nyumtolic.nyumtolic.service.CategoryService;
//import com.nyumtolic.nyumtolic.service.RestaurantService;
//import lombok.RequiredArgsConstructor;
//import org.apache.poi.ss.usermodel.*;
//import org.apache.poi.xssf.usermodel.XSSFWorkbook;
//import org.springframework.boot.CommandLineRunner;
//import org.springframework.core.io.ClassPathResource;
//import org.springframework.stereotype.Component;
//
//import java.io.InputStream;
//import java.util.ArrayList;
//import java.util.Iterator;
//import java.util.List;
//
//@Component
//@RequiredArgsConstructor
//public class ExcelDataLoader implements CommandLineRunner {
//
//    private final CategoryService categoryService;
//    private final RestaurantService restaurantService;
//
//    @Override
//    public void run(String... args) throws Exception {
//        InputStream in = new ClassPathResource("Data/nyumData.xlsx").getInputStream();
//        Workbook workbook = new XSSFWorkbook(in);
//
//        Sheet categorySheet = workbook.getSheet("Category");
//        Sheet restaurantSheet = workbook.getSheet("Restaurant");
//
//        // 카테고리 데이터 동기화
//        syncCategories(categorySheet);
//
//        // 레스토랑 데이터 동기화
//        syncRestaurants(restaurantSheet);
//
//        workbook.close();
//    }
//
//
//    private void syncCategories(Sheet categorySheet) {
//        List<Category> existingCategories = categoryService.findAll();  // 기존 카테고리 목록 불러오기
//        List<Category> loadedCategories = new ArrayList<>();  // 엑셀에서 로드된 카테고리 저장 목록
//
//        Iterator<Row> rowIterator = categorySheet.iterator();
//
//        if (rowIterator.hasNext()) {
//            rowIterator.next(); // 헤더 건너뛰기
//        }
//
//        while (rowIterator.hasNext()) {
//            Row row = rowIterator.next();
//            String categoryName = row.getCell(0).getStringCellValue();
//            boolean isMainCategory = row.getCell(1).getBooleanCellValue();
//
//            // 카테고리 이름으로 기존 데이터 확인
//            Category category = categoryService.findByName(categoryName);
//            if (category == null) {
//                // 새로운 카테고리 추가
//                category = new Category();
//                category.setName(categoryName);
//            }
//
//            // 카테고리 정보 업데이트
//            category.setMainCategory(isMainCategory);
//            categoryService.save(category);  // 새로운 카테고리 추가 또는 업데이트
//
//            loadedCategories.add(category);  // 로드된 카테고리 목록에 추가
//        }
//
//        // 기존 데이터 중 Excel에 없는 카테고리 삭제
//        for (Category existingCategory : existingCategories) {
//            if (!loadedCategories.contains(existingCategory)) {
//                // 카테고리를 참조하는 레스토랑에서 이 카테고리를 제거
//                for (Restaurant restaurant : existingCategory.getRestaurants()) {
//                    restaurant.getCategories().remove(existingCategory);
//                    restaurantService.save(restaurant);  // 참조 업데이트
//                }
//                categoryService.delete(existingCategory);  // 카테고리 삭제
//            }
//        }
//    }
//
//    private void syncRestaurants(Sheet restaurantSheet) {
//        List<Restaurant> existingRestaurants = restaurantService.findAll();  // 기존 레스토랑 목록 불러오기
//        List<Restaurant> loadedRestaurants = new ArrayList<>();  // 엑셀에서 로드된 레스토랑 저장 목록
//
//        Iterator<Row> rowIterator = restaurantSheet.iterator();
//
//        // 첫 번째 행은 헤더이므로 건너뜁니다.
//        if (rowIterator.hasNext()) {
//            rowIterator.next();
//        }
//
//        while (rowIterator.hasNext()) {
//            Row row = rowIterator.next();
//            String restaurantName = row.getCell(0).getStringCellValue();
//
//            // 레스토랑 이름으로 기존 데이터 확인
//            Restaurant restaurant = restaurantService.findByName(restaurantName);
//            if (restaurant == null) {
//                // 새로운 레스토랑 추가
//                restaurant = new Restaurant();
//                restaurant.setName(restaurantName);
//            }
//
//            // 레스토랑 정보 업데이트
//            restaurant.setAddress(row.getCell(1).getStringCellValue());
//            restaurant.setPhoneNumber(row.getCell(2).getStringCellValue());
//            restaurant.setRating(row.getCell(3).getNumericCellValue());
//            restaurant.setDescription(row.getCell(5).getStringCellValue());
//            restaurant.setTravelTime((int) row.getCell(6).getNumericCellValue());
//            restaurant.setLatitude(row.getCell(8).getNumericCellValue());
//            restaurant.setLongitude(row.getCell(9).getNumericCellValue());
//            restaurant.setPhoto(row.getCell(10).getStringCellValue());
//
//            // 메뉴 업데이트
//            String menuName = row.getCell(4).getStringCellValue();
//            List<String> menuNames = new ArrayList<>();
//            for (String name : menuName.split(",")) {
//                if (name != null && !name.isEmpty()) {
//                    menuNames.add(name.trim());
//                }
//            }
//            restaurant.setMenu(menuNames);
//
//            // 카테고리 업데이트
//            String categoryName = row.getCell(7).getStringCellValue();
//            List<Category> categories = new ArrayList<>();
//            for (String name : categoryName.split(",")) {
//                Category category = categoryService.findByName(name.trim());
//                if (category != null) {
//                    categories.add(category);
//                }
//            }
//            restaurant.setCategories(categories);
//
//            // 새로운 레스토랑 추가 또는 기존 레스토랑 업데이트
//            restaurantService.save(restaurant);
//            loadedRestaurants.add(restaurant);  // 로드된 레스토랑 목록에 추가
//        }
//
//        // 기존 데이터 중 Excel에 없는 레스토랑 삭제
//        for (Restaurant existingRestaurant : existingRestaurants) {
//            if (!loadedRestaurants.contains(existingRestaurant)) {
//                restaurantService.delete(existingRestaurant);  // 레스토랑 삭제
//            }
//        }
//    }
//}
