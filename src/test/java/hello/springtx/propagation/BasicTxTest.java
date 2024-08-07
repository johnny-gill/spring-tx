package hello.springtx.propagation;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.interceptor.DefaultTransactionAttribute;

import javax.sql.DataSource;

@SpringBootTest
@Slf4j
public class BasicTxTest {

    @Autowired
    PlatformTransactionManager txManager;

    @TestConfiguration
    static class Config {
        @Bean
        PlatformTransactionManager platformTransactionManager(DataSource dataSource) {
            return new DataSourceTransactionManager(dataSource);
        }
    }

    @Test
    void 커밋() {
        log.info("START commit");
        TransactionStatus transactionStatus = txManager.getTransaction(new DefaultTransactionAttribute());
        txManager.commit(transactionStatus);
        log.info("END commit");
    }

    @Test
    void 롤백() {
        log.info("START rollback");
        TransactionStatus transactionStatus = txManager.getTransaction(new DefaultTransactionAttribute());
        txManager.rollback(transactionStatus);
        log.info("END rollback");
    }

    @Test
    void 커밋_커밋() {
        log.info("START commit1");
        TransactionStatus txStatus1 = txManager.getTransaction(new DefaultTransactionAttribute());
        txManager.commit(txStatus1);
        log.info("END commit1");

        log.info("START commit2");
        TransactionStatus txStatus2 = txManager.getTransaction(new DefaultTransactionAttribute());
        txManager.commit(txStatus2);
        log.info("END commit2");
    }

    @Test
    void 커밋_롤백() {
        log.info("START commit");
        TransactionStatus tx1 = txManager.getTransaction(new DefaultTransactionAttribute());
        txManager.commit(tx1);
        log.info("END commit");

        log.info("START rollback");
        TransactionStatus tx2 = txManager.getTransaction(new DefaultTransactionAttribute());
        txManager.rollback(tx2);
        log.info("END rollback");
    }
}
