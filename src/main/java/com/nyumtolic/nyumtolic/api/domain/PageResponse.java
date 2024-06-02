package com.nyumtolic.nyumtolic.api.domain;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.springframework.data.domain.Page;

import java.util.List;

@Data
public class PageResponse<E> {
    @Schema(description =  "현재 페이지 번호", example = "3")
    private long currentPage;

    @Schema(description = "전체 페이지 수", example = "20")
    private long totalPages;

    @Schema(description = "전체 음식점 개수", example = "300")
    private long totalElements;

    @Schema(description = "음식점 목록")
    private List<E> content;

    public PageResponse(Page<E> page) {
        currentPage = page.getNumber();
        totalPages = page.getTotalPages();
        totalElements = page.getTotalElements();
        content = page.getContent();
    }
}
