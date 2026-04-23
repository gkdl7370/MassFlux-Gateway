package com.massflux.gateway;

import com.massflux.gateway.core.NettyServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class GatewayApplication {

	private static final Logger log = LoggerFactory.getLogger(GatewayApplication.class);

	public static void main(String[] args) {
		SpringApplication.run(GatewayApplication.class, args);
	}

	@Bean
	public CommandLineRunner runner(
			NettyServer nettyServer,
			@Value("${gateway.port}") int port) {
		return args -> {
			// 메인 스레드 블로킹 리스크 헷지를 위한 별도 스레드 할당
			Thread nettyThread = new Thread(() -> {
				try {
					nettyServer.start(port);
				} catch (Exception e) {
					log.error("[System Risk] Netty Server Startup Failed", e);
				}
			}, "Netty-Server-Thread");

			nettyThread.setDaemon(false);
			nettyThread.start();
		};
	}
}