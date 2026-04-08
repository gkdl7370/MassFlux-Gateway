package com.massflux.gateway;

import com.massflux.gateway.core.NettyServer;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class GatewayApplication {

	public static void main(String[] args) {
		SpringApplication.run(GatewayApplication.class, args);
	}

	@Bean
	public CommandLineRunner runner(NettyServer nettyServer) {
		return args -> {
			// Netty 서버를 8003 포트로 시작
			nettyServer.start(8003);
		};
	}
}