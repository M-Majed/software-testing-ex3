package com.iut;

import com.iut.account.model.Account;
import com.iut.account.service.AccountService;
import com.iut.user.service.UserService;

import java.util.List;

public class BankService {
    private final UserService userService;
    private final AccountService accountService;

    public BankService(final UserService userService, final AccountService accountService) {
        this.userService = userService;
        this.accountService = accountService;
    }

    public List<Account> getUserAccounts(String userId) {
        return accountService.getUserAccounts(userId);
    }


    // TODO implement methods from BankServiceTest


}
