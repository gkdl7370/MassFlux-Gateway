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
@RequiredArgsConstructor // 생성자 주입을 자동으로 생성해줍니다.
public class NettyServer {
    private final ConfigManager configManager;
    private final RestApiService apiService;

    private EventLoopGroup bossGroup;
    private EventLoopGroup workerGroup;

    public void start(int port) {
        bossGroup = new NioEventLoopGroup(1);
        workerGroup = new NioEventLoopGroup();

        try {
            ServerBootstrap b = new ServerBootstrap();
            b.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        public void initChannel(SocketChannel ch) {
                            // 주입받은 서비스들을 핸들러에 전달합니다.
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