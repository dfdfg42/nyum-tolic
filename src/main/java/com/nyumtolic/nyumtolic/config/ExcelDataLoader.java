package com.nyumtolic.nyumtolic.config;
import com.nyumtolic.nyumtolic.domain.Category;
import com.nyumtolic.nyumtolic.domain.Restaurant;
import com.nyumtolic.nyumtolic.repository.CategoryRepository;
import com.nyumtolic.nyumtolic.service.CategoryService;
import com.nyumtolic.nyumtolic.service.MenuService;
import com.nyumtolic.nyumtolic.service.RestaurantService;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


@Component
@RequiredArgsConstructor
public class ExcelDataLoader implements CommandLineRunner {



    private final CategoryService categoryService;
    private final RestaurantService restaurantService;

    @Override
    public void run(String... args) throws Exception {
        InputStream in = new ClassPathResource("Data/nyumData.xlsx").getInputStream();
        Workbook workbook = new XSSFWorkbook(in);

        Sheet categorySheet = workbook.getSheet("Category");
        Sheet restaurantSheet = workbook.getSheet("Restaurant");

        // 카테고리 데이터 로드 및 동기화
        syncCategories(categorySheet);

        // 레스토랑 데이터 로드 및 동기화
        syncRestaurants(restaurantSheet);

        workbook.close();
    }


    private void syncCategories(Sheet categorySheet) {
        List<Category> existingCategories = categoryService.findAll();
        List<Category> loadedCategories = new ArrayList<>();

        Iterator<Row> rowIterator = categorySheet.iterator();
        if (rowIterator.hasNext()) {
            rowIterator.next();
        }

        while (rowIterator.hasNext()) {
            Row row = rowIterator.next();

            String categoryName = row.getCell(0).getStringCellValue();
            boolean isMainCategory = row.getCell(1).getBooleanCellValue();

            Category category = categoryService.findByName(categoryName);
            if (category == null) {
                category = new Category();
                category.setName(categoryName);
            }

            category.setMainCategory(isMainCategory);
            categoryService.save(category);
            loadedCategories.add(category);
        }

        // 기존 데이터베이스에 있지만 Excel에는 없는 카테고리 삭제
        for (Category existingCategory : existingCategories) {
            if (!loadedCategories.contains(existingCategory)) {
                categoryService.delete(existingCategory);
            }
        }
    }

    private void syncRestaurants(Sheet restaurantSheet) {
        List<Restaurant> existingRestaurants = restaurantService.findAll();
        List<Restaurant> loadedRestaurants = new ArrayList<>();

        Iterator<Row> rowIterator = restaurantSheet.iterator();
        if (rowIterator.hasNext()) {
            rowIterator.next();
        }

        while (rowIterator.hasNext()) {
            Row row = rowIterator.next();

            String restaurantName = row.getCell(0).getStringCellValue();

            Restaurant restaurant = restaurantService.findByName(restaurantName);
            if (restaurant == null) {
                restaurant = new Restaurant();
                restaurant.setName(restaurantName);
            }

            restaurant.setAddress(row.getCell(1).getStringCellValue());
            restaurant.setPhoneNumber(row.getCell(2).getStringCellValue());
            restaurant.setRating(row.getCell(3).getNumericCellValue());
            restaurant.setDescription(row.getCell(5).getStringCellValue());
            restaurant.setTravelTime((int) row.getCell(6).getNumericCellValue());
            restaurant.setLatitude(row.getCell(8).getNumericCellValue());
            restaurant.setLongitude(row.getCell(9).getNumericCellValue());
            restaurant.setPhoto(row.getCell(10).getStringCellValue());


            // 메뉴 업데이트
            String menuName = row.getCell(4).getStringCellValue();
            List<String> menuNames = new ArrayList<>();
            for (String name : menuName.split(",")) {
                if (name != null) {
                    menuNames.add(name.trim());
                }
            }
            restaurant.setMenu(menuNames);

            // 카테고리 업데이트
            String categoryName = row.getCell(7).getStringCellValue();
            List<Category> categories = new ArrayList<>();
            for (String name : categoryName.split(",")) {
                Category category = categoryService.findByName(name.trim());
                if (category != null) {
                    categories.add(category);
                }
            }

            restaurant.setCategories(categories);

            restaurantService.save(restaurant);
            loadedRestaurants.add(restaurant);
        }

        // 기존 데이터베이스에 있지만 Excel에는 없는 레스토랑 삭제
        for (Restaurant existingRestaurant : existingRestaurants) {
            if (!loadedRestaurants.contains(existingRestaurant)) {
                restaurantService.delete(existingRestaurant);
            }
        }
    }
}






<<<<<<< HEAD
=======

>>>>>>> s3ver
