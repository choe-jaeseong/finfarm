package com.moneygang.finfarm.domain.banking.dto.request;

import lombok.Getter;

@Getter
public class BankingPasswordChangeRequest {
    private Integer changePassword;
    private Integer checkPassword;
}
