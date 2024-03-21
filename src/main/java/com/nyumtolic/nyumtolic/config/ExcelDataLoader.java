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
        Sheet menuSheet = workbook.getSheet("Menu");

        // 카테고리 데이터 로드 및 저장
        loadAndSaveCategories(categorySheet);

        // 레스토랑 데이터 로드 및 저장
        loadAndSaveRestaurants(restaurantSheet);



        workbook.close();
    }



    private void loadAndSaveCategories(Sheet categorySheet) {
        // 카테고리 데이터 읽기 및 저장 로직 구현
        Iterator<Row> rowIterator = categorySheet.iterator();

        if (rowIterator.hasNext()) {
            rowIterator.next();
        }

        while (rowIterator.hasNext()) {
            Row row = rowIterator.next();
            Category category = new Category();
            category.setName(row.getCell(0).getStringCellValue());
            category.setMainCategory(row.getCell(1).getBooleanCellValue());
            categoryService.save(category);
        }
    }

    private void loadAndSaveRestaurants(Sheet restaurantSheet) {
        // 레스토랑 데이터 읽기 및 저장 로직 구현

        Iterator<Row> rowIterator = restaurantSheet.iterator();

        // 첫 번째 행은 헤더이므로 건너뜁니다.
        if (rowIterator.hasNext()) {
            rowIterator.next();
        }

        while (rowIterator.hasNext()) {
            Row row = rowIterator.next();
            Restaurant restaurant = new Restaurant();
            restaurant.setName(row.getCell(0).getStringCellValue());

            restaurant.setAddress(row.getCell(1).getStringCellValue());
            restaurant.setPhoneNumber(row.getCell(2).getStringCellValue());
            restaurant.setRating(row.getCell(3).getNumericCellValue());
            restaurant.setDescription(row.getCell(5).getStringCellValue());
            restaurant.setTravelTime((int) row.getCell(6).getNumericCellValue());
            restaurant.setLatitude(row.getCell(8).getNumericCellValue());
            restaurant.setLongitude(row.getCell(9).getNumericCellValue());
            restaurant.setPhoto(row.getCell(10).getStringCellValue());

            // 메뉴 이름을 기반으로 메뉴에 추가

            String menuName = row.getCell(4).getStringCellValue();
            List<String> menuNames = new ArrayList<>();

            for (String name: menuName.split(",")){
                if (name !=null){
                    menuNames.add(name);
                }
            }
            if (!menuNames.isEmpty()) {
                restaurant.setMenu(menuNames);
            }

            // 카테고리 이름을 기반으로 카테고리 엔티티를 찾습니다.
            String categoryName = row.getCell(7).getStringCellValue();
            List<Category> categories = new ArrayList<>();

            // ','를 기준으로 카테고리 이름을 분리합니다(여러 카테고리 지원).
            for (String name : categoryName.split(",")) {
                Category category = categoryService.findByName(name.trim());
                if (category != null) {
                    categories.add(category);
                }
            }

            // 찾은 카테고리들을 레스토랑에 설정합니다.
            if (!categories.isEmpty()) {
                restaurant.setCategories(categories);
                restaurantService.save(restaurant);
            }
        }
    }
}

