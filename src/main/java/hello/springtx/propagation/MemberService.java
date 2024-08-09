package hello.springtx.propagation;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;
    private final LogRepository logRepository;

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

    public void joinV2(String username) {
        Member member = new Member(username);
        Log logMessage = new Log(username);

        log.info("start memberRepository.save");
        memberRepository.save(member);
        log.info("end memberRepository.save");

        log.info("start logRepository.save");
        try {
            logRepository.save(logMessage);
        } catch (RuntimeException e) {
            log.info("log 저장 실패냥. {}", logMessage.getMessage());
        }
        log.info("end logRepository.save");
    }
}
