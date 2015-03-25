package nio;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.util.concurrent.CountDownLatch;


public class AIOServer {
    public static void main(String[] args) {
        AsyncTimeServerHandler timeServer = new AsyncTimeServerHandler(9999);
        new Thread(timeServer).start();
    }
}

class AsyncTimeServerHandler implements Runnable {
    
    private int port;
    private CountDownLatch latch;
    private AsynchronousServerSocketChannel aioServerSocketChannel;
    
    public AsyncTimeServerHandler(int port) {
        this.port = port;
        try {
            aioServerSocketChannel = AsynchronousServerSocketChannel.open();
            aioServerSocketChannel.bind(new InetSocketAddress(port));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    @Override
    public void run() {
        latch = new CountDownLatch(1);
        doAccept();
        try {
            latch.wait();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void doAccept() {
        aioServerSocketChannel.accept(this, new AcceptCompletionHandler());
    }
}

class AcceptCompletionHandler implements CompletionHandler<AsynchronousSocketChannel, AsyncTimeServerHandler> {

    @Override
    public void completed(AsynchronousSocketChannel result, AsyncTimeServerHandler attachment) {
        
    }

    @Override
    public void failed(Throwable exc, AsyncTimeServerHandler attachment) {
        
    }

    
}