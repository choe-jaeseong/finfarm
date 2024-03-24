package com.moneygang.finfarm.domain.banking.service;

import com.moneygang.finfarm.domain.banking.entity.Loan;
import com.moneygang.finfarm.domain.banking.repository.LoanRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class LoanServiceImpl implements LoanService {

    private final LoanRepository loanRepository;

    @Override
    public void makeLoan(Loan loan) {

    }

    @Override
    public void updateLoan(Loan loan) {

    }

    @Override
    public void removeLoan(Long loanPk) {

    }
}
