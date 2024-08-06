package hello.springtx.apply;

import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
public class RollbackTest {

    @Autowired
    RollbackService rollbackService;

    @Test
    void runtimeException() {
        assertThatThrownBy(() -> rollbackService.runtimeException()).isInstanceOf(RuntimeException.class);
    }

    @Test
    void checkedException() {
        assertThatThrownBy(() -> rollbackService.checkedException()).isInstanceOf(Exception.class);
    }

    @Test
    void checkedExceptionRollbackFor() {
        assertThatThrownBy(() -> rollbackService.checkedExceptionRollbackFor()).isInstanceOf(Exception.class);
    }


    @TestConfiguration
    static class RollbackTestConfig {
        @Bean
        RollbackService rollbackService() {
            return new RollbackService();
        }
    }

    @Slf4j
    static class RollbackService {

        // rollback
        @Transactional
        public void runtimeException() {
            log.info("CALL runtimeException()");
            throw new RuntimeException();
        }

        // commit
        @Transactional
        public void checkedException() throws MyException {
            log.info("CALL checkedException()");
            throw new MyException();
        }

        // rollback
        @Transactional(rollbackFor = MyException.class)
        public void checkedExceptionRollbackFor() throws MyException {
            log.info("CALL checkedExceptionRollbackFor()");
            throw new MyException();
        }
    }

    static class MyException extends Exception {
    }
}
