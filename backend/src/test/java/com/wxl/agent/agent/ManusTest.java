package com.wxl.agent.agent;

import jakarta.annotation.Resource;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import reactor.core.publisher.Flux;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class ManusTest {

    @Resource
    private Manus manus;

    @Test
    void run() {
        String userPrompt = """
                我的另一半居住在上海静安区，请帮我找到 5 公里内合适的约会地点，
                并结合一些网络图片，制定一份详细的约会计划，
                并以 PDF 格式输出""";
//        String answer = manus.run(userPrompt);
//        Assertions.assertNotNull(answer);
        Flux<String> stringFlux = manus.runStream(userPrompt, "123");

    }
}