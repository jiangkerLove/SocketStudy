package com.study.libclink.core;

import java.io.Closeable;
import java.nio.channels.SocketChannel;

/**
 * 提供者模式接口(观察者模式)
 * 并非针对某一个链接，而是所有链接都可以通过这个监听
 * 提供注册和取消注册的方法，并把注册的具体实现放在Runnable中，方便后面使用线程池调用
 */
public interface IoProvider extends Closeable {

    //注册输入,想要通过异步的模式从channel中读取数据，当有消息时callback回调
    boolean registerInput(SocketChannel channel,HandlerInputCallback callback);

    /**
     * 当想要发送数据时，不知道现在是否支持发送数据，提前把数据放入attach中，
     * @param callback 当可以发送数据时，通过callback回调通知已经可以发送数据了
     */
    boolean registerOutput(SocketChannel channel,HandlerOutputCallback callback);

    void unRegisterInput(SocketChannel channel);

    void unRegisterOutput(SocketChannel channel);

    abstract class HandlerInputCallback implements Runnable{

        @Override
        public void run() {
            canProviderInput();
        }

        protected abstract void canProviderInput();
    }

    abstract class HandlerOutputCallback implements Runnable{
        private Object attach;

        @Override
        public final void run() {
            canProviderOutput(attach);
        }

        public final void setAttach(Object attach){
            this.attach = attach;
        }

        protected abstract void canProviderOutput(Object attach);
    }
}
