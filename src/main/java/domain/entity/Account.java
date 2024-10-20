package domain.entity;

import java.math.BigDecimal;
import java.util.concurrent.locks.ReentrantLock;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Builder
@Getter
@Setter
public class Account {
    private final String accountId;
    private BigDecimal balance;
    private final ReentrantLock lock = new ReentrantLock();

    public void lock() {
        lock.lock();
    }

    public void unlock() {
        lock.unlock();
    }
}
