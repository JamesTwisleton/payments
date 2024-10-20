package domain.repository;

import domain.entity.Account;
import domain.entity.Payment;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class InMemoryAccountRepository implements AccountRepository {
  private final Map<String, Account> accounts = new ConcurrentHashMap<>();
  private static final String ACCOUNT_ID_0 = "ACCOUNT_ID_0";
  private static final String ACCOUNT_ID_1 = "ACCOUNT_ID_1";
  private static final String ACCOUNT_ID_2 = "ACCOUNT_ID_2";
  private static final String ACCOUNT_ID_3 = "ACCOUNT_ID_3";
  private static final String ACCOUNT_ID_4 = "ACCOUNT_ID_4";
  private static final String ACCOUNT_ID_5 = "ACCOUNT_ID_5";
  private static final String ACCOUNT_ID_6 = "ACCOUNT_ID_6";
  private static final String ACCOUNT_ID_7 = "ACCOUNT_ID_7";
  private static final String ACCOUNT_ID_8 = "ACCOUNT_ID_8";
  private static final String ACCOUNT_ID_9 = "ACCOUNT_ID_9";
  private static final BigDecimal TEN_THOUSAND_BALANCE = new BigDecimal("10000.00");
  private static final BigDecimal ZERO_BALANCE = new BigDecimal("0.00");

  public InMemoryAccountRepository() {
    accounts.put(
        ACCOUNT_ID_0,
        Account.builder().accountId(ACCOUNT_ID_0).balance(TEN_THOUSAND_BALANCE).build());
    accounts.put(
        ACCOUNT_ID_1,
        Account.builder().accountId(ACCOUNT_ID_1).balance(TEN_THOUSAND_BALANCE).build());
    accounts.put(
        ACCOUNT_ID_2,
        Account.builder().accountId(ACCOUNT_ID_2).balance(TEN_THOUSAND_BALANCE).build());
    accounts.put(
        ACCOUNT_ID_3,
        Account.builder().accountId(ACCOUNT_ID_3).balance(TEN_THOUSAND_BALANCE).build());
    accounts.put(
        ACCOUNT_ID_4,
        Account.builder().accountId(ACCOUNT_ID_4).balance(TEN_THOUSAND_BALANCE).build());
    accounts.put(
        ACCOUNT_ID_5, Account.builder().accountId(ACCOUNT_ID_5).balance(ZERO_BALANCE).build());
    accounts.put(
        ACCOUNT_ID_6, Account.builder().accountId(ACCOUNT_ID_6).balance(ZERO_BALANCE).build());
    accounts.put(
        ACCOUNT_ID_7, Account.builder().accountId(ACCOUNT_ID_7).balance(ZERO_BALANCE).build());
    accounts.put(
        ACCOUNT_ID_8, Account.builder().accountId(ACCOUNT_ID_8).balance(ZERO_BALANCE).build());
    accounts.put(
        ACCOUNT_ID_9, Account.builder().accountId(ACCOUNT_ID_9).balance(ZERO_BALANCE).build());
  }

  @Override
  public Optional<Account> findByAccountId(String accountId) {
    return Optional.ofNullable(accounts.get(accountId));
  }

  @Override
  public Map<String, Account> findAll() {
    return accounts;
  }
 }
