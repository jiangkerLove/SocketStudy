package com.study.libclink.core;

import com.study.libclink.box.StringReceivePacket;
import com.study.libclink.box.StringSendPacket;
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
    private SendDispatcher sendDispatcher;
    private ReceiveDispatcher receiveDispatcher;

    public void setUp(SocketChannel socketChannel) throws IOException {
        this.channel = socketChannel;
        IoContext context = IoContext.get();
        SocketChannelAdapter adapter = new SocketChannelAdapter(channel, context.getIoProvider(), this);
        this.sender = adapter;
        this.receiver = adapter;
        sendDispatcher = new AsyncSendDispatcher(sender);
        receiveDispatcher = new AsyncReceiveDispatcher(receiver, receivePacketCallback);
        //接收启动
        receiveDispatcher.start();
    }

    public void send(String msg){
        SendPacket packet = new StringSendPacket(msg);
        sendDispatcher.send(packet);
    }

    @Override
    public void close() throws IOException {
        receiveDispatcher.close();
        sendDispatcher.close();
        sender.close();
        receiver.close();
        channel.close();
    }

    @Override
    public void onChannelClosed(SocketChannel channel) {

    }

    protected void onReceiveNewMessage(String str) {
        System.out.println(key.toString() + ": " + str);
    }

    private final ReceiveDispatcher.ReceivePacketCallback receivePacketCallback = packet -> {
        if (packet instanceof StringReceivePacket){
            String msg = ((StringReceivePacket) packet).string();
            onReceiveNewMessage(msg);
        }
    };
}
