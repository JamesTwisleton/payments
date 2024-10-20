package domain.repository;

import domain.entity.Account;
import java.util.Map;
import java.util.Optional;

public interface AccountRepository {

  Optional<Account> findByAccountId(String userId);

  Map<String, Account> findAll();
}
