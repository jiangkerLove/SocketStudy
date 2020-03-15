package com.study.libclink.core;

import com.study.libclink.impl.SocketChannelAdapter;

import java.io.Closeable;
import java.io.IOException;
import java.nio.channels.SocketChannel;
import java.util.UUID;

/**
 * 对外提供类，里面初始化SocketChannelAdapter，核心中转的中心，调度
 */
public class Connector implements Closeable, SocketChannelAdapter.OnChannelStatusChangedListener {

    private UUID key = UUID.randomUUID();
    private SocketChannel channel;
    /**
     * 数据先经过Sender和Receiver处理，然后再经过SocketChannel发送
     */
    private Sender sender;
    private Receiver receiver;

    public void setUp(SocketChannel socketChannel) throws IOException {
        this.channel = socketChannel;
        IoContext context = IoContext.get();
        SocketChannelAdapter adapter = new SocketChannelAdapter(channel, context.getIoProvider(), this);
        this.sender = adapter;
        this.receiver = adapter;

        readNextMessage();
    }

    private void readNextMessage() {
        if (receiver != null) {
            try {
                receiver.receiveAsync(echoReceiveListener);
            } catch (IOException e) {
                System.out.println("开始接收数据异常：" + e.getMessage());
            }
        }
    }

    @Override
    public void close() throws IOException {
        channel.close();
    }

    @Override
    public void onChannelClosed(SocketChannel channel) {

    }

    private IoArgs.IoArgsEventListener echoReceiveListener = new IoArgs.IoArgsEventListener() {
        @Override
        public void onStarted(IoArgs args) {

        }

        @Override
        public void onCompleted(IoArgs args) {
            //打印
            onReceiveNewMessage(args.bufferString());
            //读取下一条数据
            readNextMessage();
        }
    };

    protected void onReceiveNewMessage(String str) {
        System.out.println(key.toString() + ": " + str);
    }
}
