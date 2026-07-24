package org.mtech.ledger.common.uuid;

import java.util.UUID;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
public class UuidGeneratorConfiguration {

    @Bean
    public UuidGenerator uuidGenerator() {
        return UUID::randomUUID;
    }
}
