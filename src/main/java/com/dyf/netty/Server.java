package com.dyf.netty;

import com.dyf.netty.NettyServer.HttpServerInitializer;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
/**
 * @author dyf
 * @Time 2021/06/05
 * @Description netty
 */
public class Server {

    private final static int  port = 8080;

    public static void main(String[] args) throws InterruptedException {
        NioEventLoopGroup boss = new NioEventLoopGroup();
        NioEventLoopGroup work = new NioEventLoopGroup();

        try {
            ServerBootstrap serverBootstrap = new ServerBootstrap();
            serverBootstrap.group(boss, work)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new HttpServerInitializer())
                    .option(ChannelOption.SO_BACKLOG, 128)
                    .childOption(ChannelOption.SO_KEEPALIVE, true);
            ChannelFuture future = serverBootstrap.bind(port).sync();
            System.out.println("Netty服务器启动成功...端口为"+port);

            // 等待服务器  socket 关闭 。
            future.channel().closeFuture().sync();
        } finally {
            work.shutdownGracefully();
            boss.shutdownGracefully();
        }
    }
}