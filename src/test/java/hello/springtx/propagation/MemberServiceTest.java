package hello.springtx.propagation;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.UnexpectedRollbackException;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.lang.reflect.Proxy;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Slf4j
@SpringBootTest
public class MemberServiceTest {

    @Autowired
    MemberService memberService;
    @Autowired
    MemberRepository memberRepository;
    @Autowired
    LogRepository logRepository;


    /**
     * MemberService : tx off
     * MemberRepository : tx on
     * LogRepository : tx on
     */
    @Test
    public void outerTxOff_success() {
        // given
        String username = "outerTxOff_success";

        // when
        memberService.joinV1(username);

        // then
        assertTrue(memberRepository
                .findByUsername(username)
                .isPresent());
        assertTrue(logRepository
                .findByMessage(username)
                .isPresent());
    }


    /**
     * MemberService : tx off
     * MemberRepository : tx on
     * LogRepository : tx on - exception
     */
    @Test
    public void outerTxOff_fail() {
        // given
        String username = "로그예외_outerTxOff_fail";

        // when
        assertThatThrownBy(() -> memberService.joinV1(username)).isInstanceOf(RuntimeException.class);

        // then
        assertTrue(memberRepository.findByUsername(username).isPresent()); // commit
        assertTrue(logRepository.findByMessage(username).isEmpty()); // rollback

        // member는 커밋이 되고 log는 롤백이 됐음. 서로 데이터가 맞지 않는 문제 발생 ====> 하나의 트랜잭션으로 묶는다
    }

    /**
     * MemberService : tx on
     * MemberRepository : tx off
     * LogRepository : tx off - exception
     */
    @Test
    public void on_off_off_ex() {
        // given
        String username = "로그예외";

        // when
        assertThatThrownBy(() -> memberService.joinV1(username)).isInstanceOf(RuntimeException.class);

        System.out.println(Proxy.isProxyClass(memberService.getClass()));
        System.out.println(Proxy.isProxyClass(memberRepository.getClass()));
        System.out.println(Proxy.isProxyClass(logRepository.getClass()));

        // then
        assertFalse(memberRepository.findByUsername(username).isPresent()); // rollback
        assertTrue(logRepository.findByMessage(username).isEmpty()); // rollback
    }

    /**
     * MemberService : tx on
     * MemberRepository : tx off
     * LogRepository : tx off
     *
     * Creating new transaction with name [hello.springtx.propagation.MemberService.joinV1]
     */
    @Test
    public void transaction_in_service_success() {
        // given
        String username = "test";

        // when
        memberService.joinV1(username);

        // then
        assertTrue(memberRepository.findByUsername(username).isPresent());
        assertTrue(logRepository.findByMessage(username).isPresent());
    }


    /**
     * MemberService : tx on ====> 여기서만 트랜잭션 생성된다.
     * MemberRepository : tx on
     * LogRepository : tx on
     */
    @Test
    public void on_on_on() {
        // given
        String username = "test";

        // when
        memberService.joinV1(username);

        // then
        assertTrue(memberRepository.findByUsername(username).isPresent());
        assertTrue(logRepository.findByMessage(username).isPresent());
    }

    /**
     * MemberService : tx on
     * MemberRepository : tx on
     * LogRepository : tx on, ex
     */
    @Test
    public void on_on_onex() {
        // given
        String username = "로그예외";

        // when
        assertThatThrownBy(() -> memberService.joinV1(username)).isInstanceOf(RuntimeException.class);

        // then
        assertTrue(memberRepository.findByUsername(username).isEmpty());
        assertTrue(logRepository.findByMessage(username).isEmpty());
    }

    /**
     * MemberService : tx on
     * MemberRepository : tx on
     * LogRepository : tx on, ex
     *
     * Purpose : 로그 저장은 실패하더라도 회원 정보는 저장시키고 싶다!!!
     *
     * 1. MemberService를 호출하면서 Transacitonal AOP proxy 호출 => 신규 Transaction 생성, 물리 Transaction 시작
     *
     * 2.1. MemberRepository를 호출하면서 Transacitonal AOP proxy 호출 => 기존 transaction 참여 (Default Propagation : REQUIRED)
     * 2.2. MemberRepository가 끝나고 Transacitonal AOP proxy 호출
     * 2.3. Transacitonal AOP proxy는 TransactionManager에 commit 요청
     * 2.4. 신규 Transaction이 아니므로 TransactionManager는 commit을 안함
     *
     * 3.1. LogRepository를 호출하면서 Transaciton AOP proxy 호출 => 기존 transaction 참여 (Default Propagation : REQUIRED)
     * 3.2. LogRepository에서 예외가 발생하여 Transacitonal AOP Proxy를 호출하며 예외를 던짐
     * 3.3. Transacitonal AOP proxy는 TransactionManager에 rollback 요청
     * 3.4. 신규 Transaction이 아니므로 TransactionManager는 rollback을 하지 않고 TransactionSynchronizationManager에 rollbackOnly를 설정
     * 3.4. Transacitonal AOP proxy는 MemberService로 예외를 던짐
     *
     * 4.1. MemberService에서 try-catch를 통해 예외를 잡고 및 정상 리턴
     * 4.2. MemberService에서 Transacitonal AOP Proxy를 호출
     * 4.3. Transacitonal AOP proxy는 TransactionManager에 commit 요청
     * 4.4. 신규 Transaction이므로 TransactionManager는 commit을 해야 하나, TransactionSynchronizationManager에 rollbackOnly가 true이므로 TransactionManager는 rollback을 함
     * 4.5. TransactionManager는 Transacitonal AOP proxy로 UnexpectedRollbackException을 던짐
     * 4.6. Transacitonal AOP proxy는 Client로 UnexpectedRollbackException을 던짐
     *
     */
    @Test
    public void test1() {
        // given
        String username = "로그예외";

        // when
        assertThatThrownBy(() -> memberService.joinV2(username)).isInstanceOf(UnexpectedRollbackException.class);

        // then
        assertTrue(memberRepository.findByUsername(username).isEmpty());
        assertTrue(logRepository.findByMessage(username).isEmpty());
    }

    /**
     * MemberService : tx on
     * MemberRepository : tx on
     * LogRepository : tx on, ex
     *
     * Purpose : 로그 저장은 실패하더라도 회원 정보는 저장시키고 싶다!!!
     *
     * LogRepository만 트랜잭션을 분리해본다.
     * 트랜잭션 분리를 위해 REQUIRES_NEW 설정
     * Suspending current transaction, creating new transaction with name [hello.springtx.propagation.LogRepository.save]
     */
    @Test
    public void test2() {
        // given
        String username = "로그예외";

        // when
        memberService.joinV2(username);

        // then
        assertTrue(memberRepository.findByUsername(username).isPresent());
        assertTrue(logRepository.findByMessage(username).isEmpty());
    }
}
