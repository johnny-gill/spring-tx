package hello.springtx.propagation;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

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
}
