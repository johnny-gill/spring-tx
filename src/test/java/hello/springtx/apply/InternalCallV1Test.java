package hello.springtx.apply;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import javax.swing.text.html.BlockView;

@Slf4j
@SpringBootTest
public class InternalCallV1Test {

    @Autowired
    CallService callService;

    @Test
    void printProxy() {
        log.info("callService.getClass()={}", callService.getClass());
        // @Transactional이 포함돼서 때문에 프록시 객체 주입
        // class hello.springtx.apply.InternalCallV1Test$CallService$$SpringCGLIB$$0
    }

    @Test
    void externalCall() {
        callService.external();
    }

    @Test
    void internalCall() {
        callService.internal();
    }

    @TestConfiguration
    static class InternalCallV1Config {

        @Bean
        CallService callService() {
            return new CallService();
        }
    }

    @Slf4j
    static class CallService {

        void external() {
            log.info("call external");
            printTxInfo();
            internal(); //this.internal() 이라 프록시 호출이 아님..
        }

        private static void printTxInfo() {
            boolean isTxActive = TransactionSynchronizationManager.isActualTransactionActive();
            log.info("isTxActive={}", isTxActive);
        }

        @Transactional
        void internal() {
            log.info("call internal");
            printTxInfo();
        }
    }
}
