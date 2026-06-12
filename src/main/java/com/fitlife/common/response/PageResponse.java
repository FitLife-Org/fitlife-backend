package com.fitlife.common.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(name = "PageResponse", description = "Dá»¯ liá»‡u phĂ¢n trang chuáº©n cá»§a há»‡ thá»‘ng FitLife")
public class PageResponse<T> {
    @Schema(description = "Trang hiá»‡n táº¡i (báº¯t Ä‘áº§u tá»« 1)", example = "1")
    private int currentPage;

    @Schema(description = "Tá»•ng sá»‘ trang", example = "12")
    private int totalPages;

    @Schema(description = "KĂ­ch thÆ°á»›c trang", example = "10")
    private int pageSize;

    @Schema(description = "Tá»•ng sá»‘ báº£n ghi", example = "115")
    private long totalElements;

    @Schema(description = "Danh sĂ¡ch dá»¯ liá»‡u cá»§a trang hiá»‡n táº¡i")
    private List<T> data;
}