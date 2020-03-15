package com.study.libclink.impl;

import com.study.libclink.core.IoArgs;
import com.study.libclink.core.IoProvider;
import com.study.libclink.core.Receiver;
import com.study.libclink.core.Sender;
import com.study.libclink.utils.CloseUtils;

import java.io.Closeable;
import java.io.IOException;
import java.nio.channels.SocketChannel;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 拿到socketChannel，并把具体实现转交给IoSelectorProvider
 */
public class SocketChannelAdapter implements Sender, Receiver, Closeable {
    //标记是否被关闭了，AtomicBoolean是一个原子操作
    private final AtomicBoolean isClosed = new AtomicBoolean(false);
    private final SocketChannel channel;
    private final IoProvider ioProvider;
    private final OnChannelStatusChangedListener listener;

    private IoArgs.IoArgsEventListener receiveIoEventListener;
    private IoArgs.IoArgsEventListener sendIoEventListener;

    public SocketChannelAdapter(SocketChannel channel, IoProvider ioProvider, OnChannelStatusChangedListener listener) throws IOException {
        this.channel = channel;
        this.ioProvider = ioProvider;
        this.listener = listener;

        channel.configureBlocking(false);
    }

    @Override
    public boolean receiveAsync(IoArgs.IoArgsEventListener listener) throws IOException {
        if (isClosed.get()) throw new IOException("Current channel is closed!");
        receiveIoEventListener = listener;

        return ioProvider.registerInput(channel, inputCallback);
    }

    @Override
    public boolean sendAsync(IoArgs args, IoArgs.IoArgsEventListener listener) throws IOException {
        if (isClosed.get()) throw new IOException("Current channel is closed!");
        sendIoEventListener = listener;
        //当前发送的数据附加到回调中
        outputCallback.setAttach(args);
        return ioProvider.registerOutput(channel, outputCallback);
    }

    @Override
    public void close() throws IOException {
        //比较当前状态是否为false，并更改为true
        if (isClosed.compareAndSet(false, true)) {
            ioProvider.unRegisterInput(channel);
            ioProvider.unRegisterOutput(channel);
            //关闭
            CloseUtils.close(channel);
            //回调当前Channel已关闭
            listener.onChannelClosed(channel);
        }
    }

    private final IoProvider.HandlerInputCallback inputCallback = new IoProvider.HandlerInputCallback() {
        @Override
        protected void canProviderInput() {
            if (isClosed.get()) return;
            IoArgs args = new IoArgs();
            //转化为局部变量
            IoArgs.IoArgsEventListener listener = SocketChannelAdapter.this.receiveIoEventListener;
            if (listener != null) {
                listener.onStarted(args);
            }
            try {
                //具体的读取操作
                if (args.read(channel) > 0 && listener != null) {
                    //读取完成回调
                    listener.onCompleted(args);
                } else {
                    //在可读的情况下没有读取到数据，应该就是出现了异常，所以主动抛出自定义异常
                    throw new IOException("Cannot read any data!");
                }
            } catch (IOException e) {
                CloseUtils.close(SocketChannelAdapter.this);
            }
        }
    };

    private final IoProvider.HandlerOutputCallback outputCallback = new IoProvider.HandlerOutputCallback() {
        @Override
        protected void canProviderOutput(Object attach) {
            if (isClosed.get()) return;
            //TODO
            sendIoEventListener.onCompleted(null);
        }
    };

    /**
     * 当前channel关闭时候的回调
     */
    public interface OnChannelStatusChangedListener {
        void onChannelClosed(SocketChannel channel);
    }
}
