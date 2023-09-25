package com.rutvik.interview.aspire.miniaspireservice.api.enums;


import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum Frequency {
    WEEKLY(7);


    private final int numberOfDays;
}
