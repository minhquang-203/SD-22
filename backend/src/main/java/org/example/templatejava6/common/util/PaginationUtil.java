package org.example.templatejava6.common.util;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

public class PaginationUtil {

    public static Pageable create(int page, int size) {
        return PageRequest.of(Math.max(page - 1, 0), size);
    }

    public static Pageable create(int page, int size, String sortBy, boolean desc) {
        int pageIndex = Math.max(page - 1, 0);
        Sort sort = desc ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        return PageRequest.of(pageIndex, size, sort);
    }

    // co sap xep
    public static Pageable create(int page, int size, Sort sort) {
        return PageRequest.of(Math.max(page - 1, 0), size, sort);
    }
}
