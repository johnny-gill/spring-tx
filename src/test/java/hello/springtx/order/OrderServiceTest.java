package hello.springtx.order;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

@Slf4j
@SpringBootTest
class OrderServiceTest {

    @Autowired
    OrderService orderService;

    @Autowired
    OrderRepository orderRepository;

    @Test
    void 완료() throws NotEnoughMoneyException {
        //given
        Order order = new Order();
        order.setUsername("정상");

        //when
        orderService.order(order);

        //then
        Long id = order.getId();
        Order findOrder = orderRepository.findById(id).get();

        assertThat(findOrder.getPayStatus()).isEqualTo("완료");
    }
    
    @Test
    void 예외() throws NotEnoughMoneyException {
        //given
        Order order = new Order();
        order.setUsername("예외");
        
        //when, then
        assertThatThrownBy(() -> orderService.order(order)).isInstanceOf(RuntimeException.class);

        //then - uncheck error니까 롤백
        Optional<Order> findOrder = orderRepository.findById(order.getId());
        assertThat(findOrder.isEmpty()).isTrue();
    }

    @Test
    void 잔고부족() {
        //given
        Order order = new Order();
        order.setUsername("잔고부족");

        //when
        try {
            orderService.order(order);
            fail("catch로 넘어갈테니 이 코드는 실행 안된다~");
        } catch (NotEnoughMoneyException e) {
            log.info("잔고부족 catch!!!!!!!!!!! 입금하시오!!");
        }

        // then - check error니까 커밋
        Order findOrder = orderRepository.findById(order.getId()).get();
        assertThat(findOrder.getPayStatus()).isEqualTo("대기");
    }
}