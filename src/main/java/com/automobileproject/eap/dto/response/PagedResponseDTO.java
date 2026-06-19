package com.automobileproject.eap.dto.response;

import lombok.*;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PagedResponseDTO<T> {

    private long dataCount;
    private List<T> dataList;
}
