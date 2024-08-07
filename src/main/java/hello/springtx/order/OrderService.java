package hello.springtx.order;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;

    @Transactional
    public void order(Order order) throws NotEnoughMoneyException {
        log.info("CALL order()");

        orderRepository.save(order);

        log.info("order.getUsername()={}", order.getUsername());
        if (order.getUsername().equals("예외")) {
            throw new RuntimeException("예외");
        } else if (order.getUsername().equals("잔고부족")) {
            order.setPayStatus("대기");
            throw new NotEnoughMoneyException("잔고부족");
        } else {
            order.setPayStatus("완료");
        }

        log.info("END order()");
    }
}
