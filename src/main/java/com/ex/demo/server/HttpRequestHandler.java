package com.ex.demo.server;

import java.util.HashMap;
import java.util.Map;

import cn.hutool.core.thread.ThreadUtil;
import cn.hutool.core.util.StrUtil;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpUtil;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.util.CharsetUtil;

/**
 *1. 一个EventLoopGroup当中包含一个或多个EventLoop
 * 2.一个EventLoop在它的整个生命周期中（任何I/O操作）都只会与唯一一个Thread进行绑定
 * 3.所有由EventLoop所处理的各种I/O事件都将在它所关联的那个Thread上进行处理
 * 4.一个channel在它的整个生命周期中只会注册在一个EventLoop上
 * 5.一个EventLoop在运行过程中，会被分配给一个或者多个Channel
 * 6.在Netty中，Channel的实现一定是线程安全的；基于此，我们可以存储一个Channel的引用，并且在需要向远程端点发送数据时，通过这个引用来调用Channel相应的方法；即使当时有很多线程都在使用它也不会出现多线程问题；而且，消息一定会按照顺序发送出去。
 * 7.我们在业务开发中，不要将长时间执行的任务放入到EventLoop的执行队列中，因为它将会一直阻塞该线程所对应的所有Channel上的其它执行任务，如果我们需要进行阻塞调用或是耗时的操作，可以把任务放到自定义的业务线程池，以免阻塞I/O操作。
 */
public class HttpRequestHandler extends SimpleChannelInboundHandler<FullHttpRequest> {

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) {
        ctx.flush();
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, FullHttpRequest req) throws Exception {
    	//100 status Continue
		if (is100ContinueExpected(req)) {
			ctx.write(new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.CONTINUE));
		}
        
        String msg = handleMessage(req);
        
       // 创建http响应
        FullHttpResponse response = new DefaultFullHttpResponse(
                                        HttpVersion.HTTP_1_1,
                                        HttpResponseStatus.OK,
                                        Unpooled.copiedBuffer(msg, CharsetUtil.UTF_8));
       // 设置头信息
        response.headers().set(HttpHeaderNames.CONTENT_TYPE, "text/html; charset=UTF-8");
        //response.headers().set(HttpHeaderNames.CONTENT_TYPE, "text/plain; charset=UTF-8");
       // 将 处理结果 write到客户端，写完成后，触发channel被flush，意味着请求处理结束
        ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
    }

	private String handleMessage(FullHttpRequest req) {
		// 获取请求的uri
        String uri = req.uri();
        if( StrUtil.containsAny(uri, "nnmm") ) {
        	ThreadUtil.safeSleep(5000); // 模拟耗时操作导致对IO通道的阻塞，实际上建议把耗时操作交给线程池异步处理，而不要影响IO通道处理
        }
        Map<String,String> resMap = new HashMap<>();
        resMap.put("method",req.method().name());
        resMap.put("uri",uri);
        String msg = "你请求uri为：" + uri;
		return msg;
	}

	private boolean is100ContinueExpected(FullHttpRequest req) {
		return HttpUtil.is100ContinueExpected(req);
	}
}
