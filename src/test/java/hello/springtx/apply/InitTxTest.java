package hello.springtx.apply;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.event.EventListener;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronizationManager;


@SpringBootTest
public class InitTxTest {

    @Autowired
    Hello hello;

    @Test
    void go() {

    }

    @TestConfiguration
    static class InitTxTestConfig {
        @Bean
        Hello hello() {
            return new Hello();
        }
    }

    @Slf4j
    static class Hello {

        // 트랜잭션 AOP보다 먼저 호출
        @PostConstruct
        @Transactional
        void initV1() {
            log.info("initV1 ====> TransactionSynchronizationManager.isActualTransactionActive()={}", TransactionSynchronizationManager.isActualTransactionActive());
        }

        // 트랜잭션 AOP까지 다 적용된 후 호출
        @EventListener(ApplicationReadyEvent.class)
        @Transactional
        void initV2() {
            log.info("initV2 ====> TransactionSynchronizationManager.isActualTransactionActive()={}", TransactionSynchronizationManager.isActualTransactionActive());
        }
    }
}
