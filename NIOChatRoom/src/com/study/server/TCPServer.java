package com.study.server;

import com.study.libclink.utils.CloseUtils;
import com.study.server.handle.ClientHandler;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TCPServer implements ClientHandler.ClientHandlerCallback {
    private final int port;
    private ClientListener mListener;
    private List<ClientHandler> clientHandlerList = new ArrayList<>();
    private final ExecutorService forwardingThreadPoolExecutor;
    private Selector selector;
    private ServerSocketChannel server;

    public TCPServer(int port) {
        this.port = port;
        // 转发线程池
        this.forwardingThreadPoolExecutor = Executors.newSingleThreadExecutor();
    }

    public boolean start() {
        try {
            //开启一个选择器，可以给选择器注册需要关注的事件
            selector = Selector.open();
            server = ServerSocketChannel.open();
            //设置为非阻塞
            server.configureBlocking(false);
            //绑定本地端口
            server.socket().bind(new InetSocketAddress(port));
            /**
             * 将一个Channel注册到选择器，当选择器触发对应关注事件时
             * 回调到Channel中，处理相关数据
             * 注册到选择器的通道必须为非阻塞状态，所以注册之前调用configureBlocking(false)切换到非阻塞状态
             * FileChannel不能用于Selector，因为  FileChannel不能切换为非阻塞模式；
             * 套接字通道可以
             * ！！！只监听客户端链接到达的消息，对发送的消息不进行监听
             */
            server.register(selector, SelectionKey.OP_ACCEPT);
            System.out.println("服务器信息：" + server.getLocalAddress().toString());
            //启动客户端监听
            mListener = new ClientListener();
            mListener.start();
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public void stop() {
        if (mListener != null) {
            mListener.exit();
        }
        //关闭选择器，注销所有关注的事件
        CloseUtils.close(server,selector);

        synchronized (TCPServer.this) {
            for (ClientHandler clientHandler : clientHandlerList) {
                clientHandler.exit();
            }
            clientHandlerList.clear();
        }

        // 停止线程池
        forwardingThreadPoolExecutor.shutdownNow();
    }

    public synchronized void broadcast(String str) {
        for (ClientHandler clientHandler : clientHandlerList) {
            clientHandler.send(str);
        }
    }

    @Override
    public synchronized void onSelfClosed(ClientHandler handler) {
        clientHandlerList.remove(handler);
    }

    @Override
    public void onNewMessageArrived(final ClientHandler handler, final String msg) {
        // 异步提交转发任务
        forwardingThreadPoolExecutor.execute(() -> {
            synchronized (TCPServer.this) {
                for (ClientHandler clientHandler : clientHandlerList) {
                    if (clientHandler.equals(handler)) {
                        // 跳过自己
                        continue;
                    }
                    // 对其他客户端发送消息
                    clientHandler.send(msg);
                }
            }
        });

    }

    private class ClientListener extends Thread {
        private boolean done = false;

        @Override
        public void run() {
            super.run();

            Selector selector = TCPServer.this.selector;

            System.out.println("服务器准备就绪～");
            // 等待客户端连接
            do {
                //得到客户端
                try {
                    /**
                     * selector.select()是一个会阻塞的方法，当有事件到达时方法返回
                     * select()/selectNow()一个通道Channel，处理一个当前的可用的
                     * 待处理的通道数据
                     * 这里判断是0是在被wakeUp时候被触发，所以这里可以关闭退出
                     */
                    if (selector.select() == 0){
                        if(done) break;
                        continue;
                    }
                    /**
                     * 得到当前就绪的通道
                     */
                    Iterator<SelectionKey> iterator = selector.selectedKeys().iterator();
                    while (iterator.hasNext() && !done) {
                        SelectionKey key = iterator.next();
                        //拿到这个key就移除掉
                        iterator.remove();
                        //检查当前key的状态是否是我们关注的
                        //客户端到达状态
                        if (key.isAcceptable()) {
                            //因为前面只注册了这一个，所以拿到的就是这个channel
                            ServerSocketChannel serverSocketChannel = (ServerSocketChannel) key.channel();
                            //非阻塞状态拿到客户端链接
                            SocketChannel socketChannel = serverSocketChannel.accept();

                            try {
                                // 客户端构建异步线程
                                ClientHandler clientHandler = new ClientHandler(socketChannel, TCPServer.this);
                                // 添加同步处理
                                synchronized (TCPServer.this) {
                                    clientHandlerList.add(clientHandler);
                                }
                            } catch (IOException e) {
                                e.printStackTrace();
                                System.out.println("客户端连接异常：" + e.getMessage());
                            }
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } while (!done);
            System.out.println("服务器已关闭！");
        }

        void exit() {
            done = true;
            //唤醒一个处于select状态的选择器，若没有事件到达，唤醒之后的select返回的是0
            selector.wakeup();
        }
    }
}
