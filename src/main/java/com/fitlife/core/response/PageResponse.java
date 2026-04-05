package com.fitlife.core.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PageResponse<T> {
    private int currentPage;    // Trang hiện tại
    private int totalPages;     // Tổng số trang
    private int pageSize;       // Số phần tử trên 1 trang
    private long totalElements; // Tổng số bản ghi trong DB
    private List<T> data;       // Danh sách dữ liệu thực tế
}