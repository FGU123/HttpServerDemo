package com.ex.demo.server;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;

/**
 * 此initializer用于初始化channel，主要用来支持批量添加若干channelHandler添加到pipline中
 */
public class HttpServerInitializer extends ChannelInitializer<SocketChannel>{

	/**
	 * 客户端每发起一次新的请求（服务每次接收到请求），都会调用这个方法
	 * 也就是说，每个请求，对应一个socketChannel，也对应一个pipeline（channel与pipeline是1对1）
	 * 而每个pipeline对应一系列的ChannelHandler，这些channelHandler以责任链的方式（双向链表）串起来，也就是每个channelHandler就是处理链条的一环
	 */
    @Override
    protected void initChannel(SocketChannel channel) throws Exception {
        ChannelPipeline pipeline = channel.pipeline();
        pipeline.addLast(new HttpServerCodec());// http 编解码
        pipeline.addLast("httpAggregator",new HttpObjectAggregator(512*1024)); // http 消息聚合器，512*1024为接收的最大contentlength
        pipeline.addLast(new HttpRequestHandler());// 实际的请求处理器
    }
}
