package org.koreait.trend.services;

import org.junit.jupiter.api.Test;
import org.koreait.trend.entities.NewsTrend;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class NewsTrendServiceTest {

    @Autowired
    private NewsTrendService service;

    @Test
    void test() {
        NewsTrend data = service.process();
        System.out.println(data);
        service.scheduledJob();
    }
}
