package nio;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;


public class NettyServer {
    public static void main(String[] args) throws InterruptedException {
        // 初始了8个NioEventLoop，每个NioEventLoop都持有一个open的Selector
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        
        MyInitChannelHandler myInitHander = new MyInitChannelHandler();
        ServerBootstrap b = new ServerBootstrap();
        // 设置父线程池与子线程池,group 与 childGroup
        b.group(bossGroup, workerGroup)
        // 通过class创建一个工厂，生产NioServerSocketChannel类，
        // NioServerSocketChannel是jdk NIO中SelectableChannel的包装类
        // NioServerSocketChannel会初始化 DefaultChannelId id, SelectableChannel ch(provider.openServerSocketChannel)、
        //     AbstractNioMessageChannel$NioMessageUnsafe unsafe, DefaultChannelPipeline pipeline等成员
            .channel(NioServerSocketChannel.class)
            // 指定初始的handler
            .childHandler(myInitHander)
            .option(ChannelOption.SO_BACKLOG, 128)
            .childOption(ChannelOption.SO_KEEPALIVE, true);
        
        /*
         * bind的过程
         * 1:使用.channel(NioServerSocketChannel.class)传入的class，生产一个NioServerSocketChannel
         * 2：新建一个 ServerBootstrap$ServerBootstrapAcceptor 实例（把childGroup(也就是上面创建的workerGroup),
         *      childHandler（也就是上面创建的MyInitHandler）），加到刚创建的channel的pipeline的末尾
         * */
        ChannelFuture f = b.bind(9999).sync();
        f.channel().closeFuture().sync();
        workerGroup.shutdownGracefully();
        bossGroup.shutdownGracefully();
    }
}

class MyInitChannelHandler extends ChannelInitializer<Channel> {
    @Override
    protected void initChannel(Channel ch) throws Exception {
        ch.pipeline().addLast(new EchoServerHander());
    }
}

class EchoServerHander extends ChannelHandlerAdapter {

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.channel().close();
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        ctx.channel().writeAndFlush(Unpooled.copiedBuffer("welcome\n".getBytes()));
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        ByteBuf buf = (ByteBuf) msg;
        byte[] bytes = new byte[buf.readableBytes()];
        buf.readBytes(bytes);
        String body = new String(bytes, "utf-8");
        System.out.print("recv : " + body);
        ByteBuf writeBuf = Unpooled.copiedBuffer("hello\n".getBytes());
        ctx.writeAndFlush(writeBuf);
    }
    
}
