package om.massflux.gateway;

import om.massflux.gateway.core.NettyServer;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class GatewayApplication {

	public static void main(String[] args) {
		SpringApplication.run(GatewayApplication.class, args);
	}

	// Spring Boot가 실행 완료된 직후 이 메서드가 호출됩니다.
	@Bean
	public CommandLineRunner runner(NettyServer nettyServer) {
		return args -> {
			// Netty 서버를 8003 포트로 시작합니다.
			nettyServer.start(8003);
		};
	}
}