package hello.springtx.order;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "orders") // order는 예약어라 orders로 생성
@Getter
@Setter
public class Order {

    @Id
    @GeneratedValue
    private Long id;

    private String username;
    private String payStatus;

    
}
