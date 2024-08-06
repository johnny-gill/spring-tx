package hello.springtx.apply;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@Slf4j
@SpringBootTest
public class InternalCallV2Test {

    @Autowired
    CallService callService;

    @Test
    void callTest() {
        callService.external();
    }

    @Test
    void printProxy() {
        // CallService는 @Transactional이 안걸려있어서 트랜잭션 프록시 없음
        log.info("callService.getClass()={}", callService.getClass());

        // InternalService는 트랜잭션 프록시 적용
        log.info("callService.internalService.getClass()={}", callService.internalService.getClass());
    }


    @TestConfiguration
    static class InternalCallV2ConfigTest {
        @Bean
        CallService callService() {
            return new CallService(internalService());
        }

        @Bean
        InternalService internalService() {
            return new InternalService();
        }
    }

    @Slf4j
    @RequiredArgsConstructor
    static class CallService {

        private final InternalService internalService;

        void external() {
            log.info("call external");
            log.info("isTxActive={}", TransactionSynchronizationManager.isActualTransactionActive());
            internalService.internal();
        }
    }

    @Slf4j
    static class InternalService {

        @Transactional
        void internal() { // private엔 트랜잭션 적용안됨
            log.info("call internal");
            log.info("isTxActive={}", TransactionSynchronizationManager.isActualTransactionActive());
        }
    }


}
