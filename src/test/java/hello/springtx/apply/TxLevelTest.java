package hello.springtx.apply;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@SpringBootTest
public class TxLevelTest {

    @Autowired
    LevelService levelService;

    @Test
    void test() {
        levelService.write();
        levelService.read();
    }

    @TestConfiguration
    static class config {

        @Bean
        LevelService levelService() {
            return new LevelService();
        }
    }

    @Slf4j
    @Transactional(readOnly = true)
    static class LevelService {

        @Transactional(readOnly = false)
        void write() {
            log.info("CALL write");
            printTxInfo();
        }

        void read() {
            log.info("CALL read");
            printTxInfo();
        }

        void printTxInfo() {
            boolean isTxActive = TransactionSynchronizationManager.isActualTransactionActive();
            boolean isTxReadOnly = TransactionSynchronizationManager.isCurrentTransactionReadOnly();

            log.info("isTxActive={}", isTxActive);
            log.info("isTxReadOnly={}", isTxReadOnly);
        }
    }
}
