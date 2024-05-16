package com.nyumtolic.nyumtolic.api.domain;

import lombok.Data;
import org.springframework.data.domain.Page;

import java.util.List;

@Data
public class PageResponse<E> {

    private long currentPage;

    private long totalPages;

    private long totalElements;

    private List<E> content;

    public PageResponse(Page<E> page) {
        currentPage = page.getNumber();
        totalPages = page.getTotalPages();
        totalElements = page.getTotalElements();
        content = page.getContent();
    }
}
