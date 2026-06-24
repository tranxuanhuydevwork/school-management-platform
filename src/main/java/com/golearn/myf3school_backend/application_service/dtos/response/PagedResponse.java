package com.golearn.myf3school_backend.application_service.dtos.response;

import lombok.Getter;
import org.springframework.data.domain.Page;

import java.util.List;

/**
 * Wrapper cho phân trang — khớp với cách JS đọc data?.data?.content
 */
@Getter
public class PagedResponse<T> {

    private final List<T>  content;
    private final int      page;
    private final int      size;
    private final long     totalElements;
    private final int      totalPages;
    private final boolean  last;

    private PagedResponse(Page<T> p) {
        this.content       = p.getContent();
        this.page          = p.getNumber();
        this.size          = p.getSize();
        this.totalElements = p.getTotalElements();
        this.totalPages    = p.getTotalPages();
        this.last          = p.isLast();
    }

    public static <T> PagedResponse<T> of(Page<T> page) {
        return new PagedResponse<>(page);
    }
}