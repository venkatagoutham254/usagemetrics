package com.aforo.billablemetrics.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UsageConditionDTO {

    private String dimension;
    private String operator;
    private String value;
}
