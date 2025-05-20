package com.ch4.lumia_backend; // 해당 파일의 패키지 선언

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing; // import 추가/확인

@EnableJpaAuditing // <<<--- 이 어노테이션을 추가!
@SpringBootApplication
public class LumiaBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(LumiaBackendApplication.class, args);
    }

}