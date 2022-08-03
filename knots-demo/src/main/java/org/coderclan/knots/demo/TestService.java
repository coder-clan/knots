package org.coderclan.knots.demo;

import org.coderclan.knots.annotation.IdempotentIdExpression;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

@Service
@IdempotentIdExpression("arg[0].requestId")
//@Idempotent
public class TestService {

    private ConcurrentHashMap<String, Integer> map = new ConcurrentHashMap<>();

    // @IdempotentIdExpression("arg[0].requestId")
    // @Idempotent
    @Transactional
    public Result<Void> increaseRewardPoint(Request<IncreaseRewardPointRequestDto> request) {
        // to test Exception
        if (Math.random() > 0.7d) {
            throw new RuntimeException("Mu ha ha ha!");
        }

        // to test method invocation need a lot of time.
        if (Math.random() > 0.7d) {
            try {
                Thread.sleep(5 * 1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        IncreaseRewardPointRequestDto dto = request.getData();
        String customerId = dto.getCustomerId().intern();
        synchronized (customerId) {
            Integer amount = map.get(customerId);

            if (Objects.isNull(amount)) {
                amount = 0;
            }
            amount += dto.getAmount();
            map.put(customerId, amount);
        }
        return new Result<>(true, null);
    }
}
