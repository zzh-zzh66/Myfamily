package com.family.myfamily.common;

import lombok.Data;
import java.util.List;

@Data
public class PageDTO<T> {
    private List<T> records;
    private Long total;
    private int page;
    private int size;

    public static <T> PageDTO<T> of(List<T> records, Long total, int page, int size) {
        PageDTO<T> dto = new PageDTO<>();
        dto.setRecords(records);
        dto.setTotal(total);
        dto.setPage(page);
        dto.setSize(size);
        return dto;
    }
}
