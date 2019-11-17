package com.ex.demo.server;

import java.net.InetSocketAddress;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LoggingHandler;

public class HttpServer {

    int port ;

    public HttpServer(int port){
        this.port = port;
    }

    public void start() throws Exception{
        ServerBootstrap bootstrap = new ServerBootstrap();
        EventLoopGroup boss = new NioEventLoopGroup();
        EventLoopGroup work = new NioEventLoopGroup();
        bootstrap.group(boss,work)
                .handler(new LoggingHandler()) // 打印debug级别日志
                .channel(NioServerSocketChannel.class) // 注册socketChannel
                .childHandler(new HttpServerInitializer()); // 初始化时挂载一个childHandler，透过initializer批量添加handler

        ChannelFuture f = bootstrap.bind(new InetSocketAddress(port)).sync();
        System.out.println(" server start up on port : " + port);
        f.channel().closeFuture().sync();
    }
    
    public static void main(String[] args) throws Exception {
    	int port = 8008;
		new HttpServer(port).start();
	}
}
