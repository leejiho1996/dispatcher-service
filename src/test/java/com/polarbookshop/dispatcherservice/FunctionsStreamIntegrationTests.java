package com.polarbookshop.dispatcherservice;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.stream.binder.test.InputDestination;
import org.springframework.cloud.stream.binder.test.OutputDestination;
import org.springframework.cloud.stream.binder.test.TestChannelBinderConfiguration;
import org.springframework.context.annotation.Import;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;

import java.io.IOException;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@SpringBootTest
@Import(TestChannelBinderConfiguration.class) // 테스트 빌더 설정
public class FunctionsStreamIntegrationTests {

    @Autowired
    private InputDestination input; // 입력 바인딩 (packlabel-in-0)

    @Autowired
    private OutputDestination output; // 출력 바인딩 (packlabel-out-0)

    @Autowired
    private ObjectMapper objectMapper; // 역직렬화를 위한 오브젝트 매퍼

    @Test
    void whenOrderAcceptedThenDispatched() throws IOException {
        long orderId = 121L;

        Message<OrderAcceptedMessage> inputMessage = MessageBuilder
                .withPayload(new OrderAcceptedMessage(orderId)).build();

        Message<OrderDispatchedMessage> expectedOutputMessage = MessageBuilder
                .withPayload(new OrderDispatchedMessage(orderId)).build();

        this.input.send(inputMessage); // 입력 채널로 메시지 전송

        // 출력 채널로 부터 메시지를 받아 확인
        assertThat(objectMapper.readValue(output.receive().getPayload(),
                OrderDispatchedMessage.class))
                .isEqualTo(expectedOutputMessage.getPayload());

    }
}
