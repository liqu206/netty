package com.dyf.netty.NettyServer;

import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.handler.codec.http.*;
import io.netty.handler.ssl.SslHandler;
import io.netty.handler.stream.ChunkedNioFile;
import io.netty.util.CharsetUtil;

import java.io.File;
import java.io.RandomAccessFile;
/**
 * @author dyf
 * @Time 2021/06/06
 * @Description netty
 */
public class HttpServerHandleAdapter extends SimpleChannelInboundHandler<FullHttpRequest> {

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, FullHttpRequest request) throws Exception {

        Channel channel = ctx.channel();

        try {
            // 状态为1xx的话，继续请求
            if (HttpUtil.is100ContinueExpected(request)) {
                FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.CONTINUE);
                ctx.writeAndFlush(response);
            }

            //处理错误或者无法解析的http请求
            if (!request.decoderResult().isSuccess()) {
                FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.BAD_REQUEST,
                        Unpooled.copiedBuffer("请求失败", CharsetUtil.UTF_8));

                response.headers().set(HttpHeaderNames.CONTENT_TYPE, "text/plain;charset=UTF-8");
                ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
            }

            String uri = request.uri();
            String path = this.getClass().getClassLoader().getResource("static").getPath() + uri;
            File file = new File(path);

            //设置不支持favicon.ico文件
            if ("/favicon.ico".equals(uri)) {
                return;
            }
            //当网页访问http://localhost:8080/的时候
            if ("/".equals(uri)) {
                FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.NOT_FOUND,
                        Unpooled.copiedBuffer("访问路径错误", CharsetUtil.UTF_8));
                response.headers().set(HttpHeaderNames.CONTENT_TYPE, "text/plain;charset=UTF-8");
                ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
                return;
            }
            //文件没有发现设置404
            if (!file.exists()) {
                FullHttpResponse response1 = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.NOT_FOUND,
                        Unpooled.copiedBuffer("访问路径错误", CharsetUtil.UTF_8));

                response1.headers().set(HttpHeaderNames.CONTENT_TYPE, "text/plain;charset=UTF-8");
                ctx.writeAndFlush(response1).addListener(ChannelFutureListener.CLOSE);
                return;
            }

            HttpResponse response = new DefaultHttpResponse(request.protocolVersion(), HttpResponseStatus.OK);
            //设置文件格式内容
            if (path.endsWith(".html")) {
                response.headers().set(HttpHeaderNames.CONTENT_TYPE, "text/html;charset=UTF-8");
            } else if (path.endsWith(".js")) {
                response.headers().set(HttpHeaderNames.CONTENT_TYPE, "application/x-javascript");
            } else if (path.endsWith(".css")) {
                response.headers().set(HttpHeaderNames.CONTENT_TYPE, "text/css;charset=UTF-8");
            }

            if (HttpUtil.isKeepAlive(request)) {
                response.headers().set(HttpHeaderNames.CONTENT_LENGTH, file.length());
                response.headers().set(HttpHeaderNames.CONNECTION, HttpHeaderValues.KEEP_ALIVE);
            }
            ctx.write(response);

            RandomAccessFile ra = new RandomAccessFile(file, "r");
            if (ctx.pipeline().get(SslHandler.class) == null) {
                ctx.write(new DefaultFileRegion(ra.getChannel(), 0, ra.length()));
            } else {
                ctx.write(new ChunkedNioFile(ra.getChannel()));
            }

            ChannelFuture channelFuture = ctx.writeAndFlush(LastHttpContent.EMPTY_LAST_CONTENT);
            if (!HttpUtil.isKeepAlive(request)) {
                channelFuture.addListener(ChannelFutureListener.CLOSE);
            }

            ra.close();
        }
        finally {//防止出现远程主机强迫关闭了一个现有的连接的bug
            ctx.close();
        }

    }
}