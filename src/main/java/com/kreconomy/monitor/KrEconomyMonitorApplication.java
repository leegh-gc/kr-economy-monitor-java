package com.kreconomy.monitor;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class KrEconomyMonitorApplication {

    public static void main(String[] args) {
        // .env 파일 로드 (없으면 무시)
        try {
            Dotenv dotenv = Dotenv.configure().ignoreIfMissing().load();
            dotenv.entries().forEach(e -> {
                if (System.getenv(e.getKey()) == null) {
                    System.setProperty(e.getKey(), e.getValue());
                }
            });
        } catch (Exception ignored) {
        }

        SpringApplication.run(KrEconomyMonitorApplication.class, args);
    }
}
