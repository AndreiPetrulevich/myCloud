package geekbrains.myCloud.netty;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.serialization.ClassResolvers;
import io.netty.handler.codec.serialization.ObjectDecoder;
import io.netty.handler.codec.serialization.ObjectEncoder;

import java.util.Map;

public class CloudServer {
    public CloudServer() {
        EventLoopGroup auth = new NioEventLoopGroup(4);
        EventLoopGroup worker = new NioEventLoopGroup();
        try {
            ServerBootstrap bootstrap = new ServerBootstrap();
            bootstrap.group(auth, worker)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel socketChannel) {
                            ChannelHandler[] handlers = {
                                    new ObjectDecoder(ClassResolvers.cacheDisabled(null)),
                                    new ObjectEncoder(),
                                    new CloudServerHandler()
                            };
                            socketChannel.pipeline().addLast(handlers);
                        }
                    });
            ChannelFuture future = bootstrap.bind(8190).sync();

            future.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            auth.shutdownGracefully();
            worker.shutdownGracefully();
        }
    }

    public static void main(String[] args) {
        Map<String, String> env = System.getenv();

        if (env.get("MONGO_CONNECTION_STRING") == null) {
            System.out.println("No connection string (MONGO_CONNECTION_STRING) in env");
            return;
        }

        if (env.get("JWT_SECRET") == null) {
            System.out.println("No JWT secret (JWT_SECRET) in env");
            return;
        }

        new CloudServer();
    }
}
