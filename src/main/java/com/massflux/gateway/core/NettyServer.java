package com.massflux.gateway.core;

import com.massflux.gateway.utils.ConfigManager;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class NettyServer {

    private final ConfigManager configManager;
    private final RestApiService apiService;

    // 역할 분리: bossGroup(연결 수락) / workerGroup(I/O 처리)
    private EventLoopGroup bossGroup;
    private EventLoopGroup workerGroup;

    public void start(int port) {
        // 스레드 1개만 할당 — 불필요한 컨텍스트 스위칭 차단
        bossGroup = new NioEventLoopGroup(1);
        workerGroup = new NioEventLoopGroup();

        try {
            ServerBootstrap b = new ServerBootstrap();
            b.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        public void initChannel(SocketChannel ch) {
                            ch.pipeline().addLast(new GatewayHandler(configManager, apiService));
                        }
                    });

            ChannelFuture f = b.bind(port).sync();
            log.info("MassFlux Gateway started on port {}", port);

            // 채널이 닫힐 때까지 블로킹
            // 이 줄이 없으면 start() 즉시 반환 -> 서버 종료
            f.channel().closeFuture().sync();

        } catch (InterruptedException e) {
            // InterruptedException은 반드시 인터럽트 플래그 복원
            Thread.currentThread().interrupt();
            log.error("Gateway 시작 중 인터럽트 발생", e);
        } catch (Exception e) {
            log.error("Gateway 시작 실패", e);
            stop();
        }
    }

    @PreDestroy
    public void stop() {
        log.info("MassFlux Gateway 종료 중...");
        if (bossGroup != null) bossGroup.shutdownGracefully();
        if (workerGroup != null) workerGroup.shutdownGracefully();
    }
}