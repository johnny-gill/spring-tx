package hello.springtx.propagation;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

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
     * MemberService : tx on
     * MemberRepository : tx off
     * LogRepository : tx off - exception
     */
    @Test
    public void test() {
        // given
        String username = "로그예외";

        // when
        assertThatThrownBy(() -> memberService.joinV1(username)).isInstanceOf(RuntimeException.class);

        // then
        assertFalse(memberRepository.findByUsername(username).isPresent()); // rollback
        assertTrue(logRepository.findByMessage(username).isEmpty()); // rollback
    }
}
