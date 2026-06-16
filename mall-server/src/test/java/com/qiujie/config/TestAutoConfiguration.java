package com.qiujie.config;

import org.mockito.Mockito;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;

/**
 * 测试环境自动配置 — 提供 mock RabbitTemplate，避免所有 @SpringBootTest 需各自声明 @MockBean。
 *
 * @author qiujie
 */
@AutoConfiguration
@Profile("test")
public class TestAutoConfiguration {

    @Bean
    public RabbitTemplate rabbitTemplate() {
        return Mockito.mock(RabbitTemplate.class);
    }
}
