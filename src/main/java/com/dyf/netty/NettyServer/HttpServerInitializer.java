package com.dyf.netty.NettyServer;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.stream.ChunkedWriteHandler;
/**
 * @author dyf
 * @Time 2021/06/07
 * @Description netty
 */
public class HttpServerInitializer extends ChannelInitializer<SocketChannel> {

    @Override
    public void initChannel(SocketChannel ch) throws Exception {
        ChannelPipeline pipeline = ch.pipeline();
        //将请求和应答消息编码或解码为HTTP消息
        pipeline.addLast(new HttpServerCodec());
        //将HTTP消息的多个部分组合成一条完整的HTTP消息
        pipeline.addLast(new HttpObjectAggregator(64 * 1024));
        pipeline.addLast(new ChunkedWriteHandler());
        pipeline.addLast(new HttpServerHandleAdapter());
    }
}