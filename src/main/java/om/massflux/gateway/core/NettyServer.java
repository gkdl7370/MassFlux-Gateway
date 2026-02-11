package om.massflux.gateway.core;

import om.massflux.gateway.utils.ConfigManager;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import jakarta.annotation.PreDestroy;

@Component
@RequiredArgsConstructor // final 필드에 대한 생성자를 자동 생성 - 불변성 확보
public class NettyServer {
    private final ConfigManager configManager;
    private final RestApiService apiService;

    // 역할 분리를 통한 I/O 병목 제거
    private EventLoopGroup bossGroup;
    private EventLoopGroup workerGroup;

    public void start(int port) {
        // 스레드를 1개만 할당하여 불필요한 컨텍스트 스위칭 차단
        bossGroup = new NioEventLoopGroup(1);
        workerGroup = new NioEventLoopGroup();

        try {
            ServerBootstrap b = new ServerBootstrap();
            b.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        public void initChannel(SocketChannel ch) {
                            // 주입받은 서비스들을 핸들러에 전달
                            ch.pipeline().addLast(new GatewayHandler(configManager, apiService));
                        }
                    });

            ChannelFuture f = b.bind(port).sync();
            System.out.println(">>> MassFlux Gateway is ready on port " + port);
        } catch (Exception e) {
            stop();
        }
    }

    @PreDestroy
    public void stop() {
        if (bossGroup != null) bossGroup.shutdownGracefully();
        if (workerGroup != null) workerGroup.shutdownGracefully();
    }
}