package com.automobileproject.eap.util;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class StandardResponseDTO {

    private int code;
    private String message;
    private Object data;
}
