package com.mainthreadlab.weinv.dto.response;

import lombok.*;

import java.util.Date;

@Setter
@Getter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ErrorResponse {

    private int status;
    private Date timestamp;
    private String message;
    private String keyError;
    private Object body;

}
