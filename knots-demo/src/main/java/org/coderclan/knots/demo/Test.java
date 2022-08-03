package org.coderclan.knots.demo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class Test {
    @Autowired
    private TestService testService;


    public void test() {
        Request<IncreaseRewardPointRequestDto> request = null;
        for (int i = 0; ; i++) {
            if (i % 3 == 0) {
                request = createDto();
            }

            try {
                final Request<IncreaseRewardPointRequestDto> req = request;
                new Thread(() -> {
                    testService.increaseRewardPoint(req);
                }).start(
                );

            } catch (Exception e) {
                e.printStackTrace();
            }

            try {
                Thread.sleep(3 * 1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private static Request<IncreaseRewardPointRequestDto> createDto() {
        IncreaseRewardPointRequestDto dto = new IncreaseRewardPointRequestDto();
        dto.setCustomerId(String.valueOf((int) (Math.random() * 10 + 1)));
        dto.setAmount((int) (Math.random() * 10 + 1));

        return new Request<>(dto, UUID.randomUUID().toString());
    }
}
