package hello.springtx.propagation;

import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.UnexpectedRollbackException;
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


    @Test
    void inner_commit() {
        log.info("start outer transaction");
        TransactionStatus outerTxStatus = txManager.getTransaction(new DefaultTransactionAttribute());
        log.info("outerTxStatus.isNewTransaction()={}", outerTxStatus.isNewTransaction());

        log.info("start inner transaction");
        TransactionStatus innerTxStatus = txManager.getTransaction(new DefaultTransactionAttribute());
        log.info("innerTxStatus.isNewTransaction()={}", innerTxStatus.isNewTransaction());

        log.info("commit inner transaction");
        txManager.commit(innerTxStatus);

        log.info("commit outer transaction");
        txManager.commit(outerTxStatus);
    }


    @Test
    void outer_rollback() {
        log.info("start outer transaction");
        TransactionStatus outerTxStatus = txManager.getTransaction(new DefaultTransactionAttribute());

        log.info("start inner transaction");
        TransactionStatus innerTxStatus = txManager.getTransaction(new DefaultTransactionAttribute());

        log.info("commit inner transaction");
        txManager.commit(innerTxStatus);

        log.info("rollback outer transaction");
        txManager.rollback(outerTxStatus);
    }


    @Test
    void inner_rollback() {
        log.info("start outer transaction");
        TransactionStatus outerTxStatus = txManager.getTransaction(new DefaultTransactionAttribute());

        log.info("start inner transaction");
        TransactionStatus innerTxStatus = txManager.getTransaction(new DefaultTransactionAttribute());

        log.info("rollback inner transaction");
        txManager.rollback(innerTxStatus); // marking existing transaction as rollback-only

        log.info("commit outer transaction");
        Assertions.assertThatThrownBy(() -> txManager.commit(outerTxStatus)).isInstanceOf(UnexpectedRollbackException.class);
    }

    @Test
    void inner_rollback_requires_new() {
        log.info("start outer tx");
        TransactionStatus outerTxStatus = txManager.getTransaction((new DefaultTransactionAttribute()));
        log.info("outerTxStatus.isNewTransaction()={}", outerTxStatus.isNewTransaction());

        log.info("start inner tx");
        DefaultTransactionAttribute definition = new DefaultTransactionAttribute(); //  TransactionDefinition 상속
        definition.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
        TransactionStatus innerTxStatus = txManager.getTransaction(definition);
        log.info("innerTxStatus.isNewTransaction()={}", innerTxStatus.isNewTransaction());

        log.info("rollback inner tx");
        txManager.rollback(innerTxStatus);

        log.info("commit outer tx");
        txManager.commit(outerTxStatus);

    }
}
