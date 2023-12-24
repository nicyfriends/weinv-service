package com.mainthreadlab.weinv.dto.request;

import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class ConfirmRequest {

    @NotNull(message = "must not be empty or null")
    private Boolean accept;

}
