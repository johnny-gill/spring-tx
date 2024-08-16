package hello.springtx.propagation;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;
    private final LogRepository logRepository;

    @Transactional
    public void joinV1(String username) {
        Member member = new Member(username);
        Log logMessage = new Log(username);

        log.info("start memberRepository.save");
        memberRepository.save(member);
        log.info("end memberRepository.save");

        log.info("start logRepository.save");
        logRepository.save(logMessage);
        log.info("end logRepository.save");
    }

    @Transactional
    public void joinV2(String username) {
        log.info("#####start memberService");

        Member member = new Member(username);
        Log logMessage = new Log(username);

        log.info("#####start memberRepository.save");
        memberRepository.save(member);
        log.info("#####end memberRepository.save");

        log.info("#####start logRepository.save");
        try {
            logRepository.save(logMessage);
        } catch (RuntimeException e) {
            log.info("log 저장 실패냥. {}", logMessage.getMessage());
        }
        log.info("#####end logRepository.save");

        log.info("#####end memberService");
    }
}
