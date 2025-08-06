package com.iut;

import com.iut.account.model.Account;
import com.iut.account.service.AccountService;
import com.iut.user.model.User;
import com.iut.user.service.UserService;

import java.util.List;
import java.util.UUID;

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

    public boolean registerNewUser(User user) {
        boolean created = userService.createUser(user);
        if (created) {
            String defaultAccountId = user.getId() + "_default";
            accountService.createAccount(defaultAccountId, 0, user.getId());
        }
        return created;
    }

    public User getUser(String id) {
        User user = userService.getUser(id);
        if (user != null) {
            List<Account> accounts = getUserAccounts(id);
            accounts.forEach(user::addAccount);
        }
        return user;
    }

    public boolean addAccountToUser(String userId, Account account) {
        if (userService.getUser(userId) == null) {
            return false;
        }
        return accountService.createAccount(account.getId(), account.getBalance(), userId);
    }

    public Account getAccount(String id) {
        return accountService.getAccount(id);
    }

    public boolean deleteAccount(String id) {
        return accountService.deleteAccount(id);
    }

}