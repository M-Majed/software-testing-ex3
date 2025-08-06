package com.iut;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import com.iut.account.model.Account;
import com.iut.account.repo.AccountRepository;
import com.iut.account.service.AccountService;

public class AccountServiceTest {

    private AccountRepository repository;
    private AccountService accountService;

    @BeforeEach
    void setup() {
        // Create a Mockito mock of AccountRepository so we don’t touch a real database
        repository = mock(AccountRepository.class);
        accountService = new AccountService(repository);
    }

    @Test
    void createAccountTest() {
        String accountId = "acc123";
        String userId = "userA";
        int initialBalance = 500;

        // Case 1: repository.existsById(accountId) returns false → should save and return true
        when(repository.existsById(accountId)).thenReturn(false);
        when(repository.save(any(Account.class))).thenReturn(true);

        boolean created = accountService.createAccount(accountId, initialBalance, userId);
        assertTrue(created, "Expected createAccount(...) to return true when id does not exist");

        // Capture the Account object passed into save(...) and verify its fields
        ArgumentCaptor<Account> captor = ArgumentCaptor.forClass(Account.class);
        verify(repository).save(captor.capture());
        Account savedAccount = captor.getValue();
        assertEquals(accountId, savedAccount.getId());
        assertEquals(initialBalance, savedAccount.getBalance());
        assertEquals(userId, savedAccount.getUserId());

        // Case 2: repository.existsById(accountId) returns true → should not save and return false
        reset(repository);
        when(repository.existsById(accountId)).thenReturn(true);

        boolean created2 = accountService.createAccount(accountId, initialBalance, userId);
        assertFalse(created2, "Expected createAccount(...) to return false when id already exists");
        verify(repository, never()).save(any(Account.class));
    }

    @Test
    void depositTest() {
        String accountId = "acc456";
        int startingBalance = 200;
        int depositAmount = 150;

        Account existing = new Account(accountId, startingBalance);
        when(repository.existsById(accountId)).thenReturn(true);
        when(repository.findById(accountId)).thenReturn(existing);
        when(repository.update(any(Account.class))).thenReturn(true);

        boolean result = accountService.deposit(accountId, depositAmount);
        assertTrue(result, "Expected deposit(...) to return true when account exists");

        // The Account object's balance should have been increased by depositAmount
        ArgumentCaptor<Account> captor = ArgumentCaptor.forClass(Account.class);
        verify(repository).update(captor.capture());
        Account updated = captor.getValue();
        assertEquals(startingBalance + depositAmount, updated.getBalance());

        // Case: depositing to a non-existent account
        reset(repository);
        when(repository.existsById(accountId)).thenReturn(false);
        boolean result2 = accountService.deposit(accountId, depositAmount);
        assertFalse(result2, "Expected deposit(...) to return false when account does not exist");
        verify(repository, never()).findById(anyString());
    }

    @Test
    void withdrawTest() {
        String accountId = "acc789";
        int startingBalance = 300;
        int withdrawAmount = 100;

        Account existing = new Account(accountId, startingBalance);
        when(repository.existsById(accountId)).thenReturn(true);
        when(repository.findById(accountId)).thenReturn(existing);
        when(repository.update(any(Account.class))).thenReturn(true);

        // Successful withdrawal
        boolean result = accountService.withdraw(accountId, withdrawAmount);
        assertTrue(result, "Expected withdraw(...) to return true when funds are sufficient");

        ArgumentCaptor<Account> captor = ArgumentCaptor.forClass(Account.class);
        verify(repository).update(captor.capture());
        Account updated = captor.getValue();
        assertEquals(startingBalance - withdrawAmount, updated.getBalance());

        // Withdrawal with insufficient funds should throw IllegalArgumentException
        reset(repository);
        when(repository.existsById(accountId)).thenReturn(true);
        Account lowBalance = new Account(accountId, 50);
        when(repository.findById(accountId)).thenReturn(lowBalance);

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            accountService.withdraw(accountId, 100);
        });
        assertEquals("Insufficient funds", exception.getMessage());

        // Withdrawal on non-existent account returns false
        reset(repository);
        when(repository.existsById(accountId)).thenReturn(false);
        boolean result2 = accountService.withdraw(accountId, withdrawAmount);
        assertFalse(result2, "Expected withdraw(...) to return false when account does not exist");
        verify(repository, never()).findById(anyString());
    }

    @Test
    void transferTest() {
        String fromId = "accFrom";
        String toId = "accTo";
        int fromBalance = 400;
        int toBalance = 100;
        int amount = 150;

        Account fromAccount = new Account(fromId, fromBalance);
        Account toAccount = new Account(toId, toBalance);

        // Case 1: Both accounts exist and sufficient funds
        when(repository.existsById(fromId)).thenReturn(true);
        when(repository.existsById(toId)).thenReturn(true);
        when(repository.findById(fromId)).thenReturn(fromAccount);
        when(repository.findById(toId)).thenReturn(toAccount);
        // repository.update(...) returns true, but transfer(...) returns true regardless of update result
        when(repository.update(any(Account.class))).thenReturn(true);

        boolean result = accountService.transfer(fromId, toId, amount);
        assertTrue(result, "Expected transfer(...) to return true when both accounts exist and funds are sufficient");

        // Verify balances have been updated: fromAccount balance decreased, toAccount increased
        ArgumentCaptor<Account> captor = ArgumentCaptor.forClass(Account.class);
        verify(repository, times(2)).update(captor.capture());
        List<Account> updatedAccounts = captor.getAllValues();
        Account updatedFrom = updatedAccounts.get(0);
        Account updatedTo = updatedAccounts.get(1);
        assertEquals(fromBalance - amount, updatedFrom.getBalance());
        assertEquals(toBalance + amount, updatedTo.getBalance());

        // Case 2: Insufficient funds should throw IllegalArgumentException
        reset(repository);
        when(repository.existsById(fromId)).thenReturn(true);
        when(repository.existsById(toId)).thenReturn(true);
        Account poorAccount = new Account(fromId, 50);
        when(repository.findById(fromId)).thenReturn(poorAccount);
        when(repository.findById(toId)).thenReturn(toAccount);

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            accountService.transfer(fromId, toId, 100);
        });
        assertEquals("Insufficient funds in source account", exception.getMessage());

        // Case 3: One or both accounts do not exist → return false
        reset(repository);
        when(repository.existsById(fromId)).thenReturn(false);
        when(repository.existsById(toId)).thenReturn(true);
        boolean result2 = accountService.transfer(fromId, toId, amount);
        assertFalse(result2, "Expected transfer(...) to return false when source account does not exist");

        reset(repository);
        when(repository.existsById(fromId)).thenReturn(true);
        when(repository.existsById(toId)).thenReturn(false);
        boolean result3 = accountService.transfer(fromId, toId, amount);
        assertFalse(result3, "Expected transfer(...) to return false when destination account does not exist");
    }

    @Test
    void getBalanceTest() {
        String accountId = "acc321";
        int balance = 750;

        // Case 1: Account exists
        when(repository.existsById(accountId)).thenReturn(true);
        when(repository.findById(accountId)).thenReturn(new Account(accountId, balance));
        int returnedBalance = accountService.getBalance(accountId);
        assertEquals(balance, returnedBalance);

        // Case 2: Account does not exist → should throw IllegalArgumentException
        reset(repository);
        when(repository.existsById(accountId)).thenReturn(false);
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            accountService.getBalance(accountId);
        });
        assertEquals("User not found", exception.getMessage());
    }

    @Test
    void existsAndGetAccountTest() {
        String accountId = "acc654";
        Account account = new Account(accountId, 900);

        // Case 1: existsById true → returns the Account
        when(repository.existsById(accountId)).thenReturn(true);
        when(repository.findById(accountId)).thenReturn(account);

        Account returned = accountService.existsAndGetAccount(accountId);
        assertNotNull(returned);
        assertEquals(accountId, returned.getId());
        assertEquals(900, returned.getBalance());

        // Case 2: existsById false → returns null
        reset(repository);
        when(repository.existsById(accountId)).thenReturn(false);
        Account returned2 = accountService.existsAndGetAccount(accountId);
        assertNull(returned2);
        verify(repository, never()).findById(anyString());
    }

    @Test
    void getAllAccountsTest() {
        Account a1 = new Account("a1", 100);
        Account a2 = new Account("a2", 200);
        List<Account> all = Arrays.asList(a1, a2);

        // Because AccountService.getAllAccounts casts repository to AccountRepository,
        // we mock repository as an AccountRepository instance (already done above).
        when(repository.findAll()).thenReturn(all);

        List<Account> returnedList = accountService.getAllAccounts();
        assertEquals(2, returnedList.size());
        assertTrue(returnedList.contains(a1));
        assertTrue(returnedList.contains(a2));
    }
}
