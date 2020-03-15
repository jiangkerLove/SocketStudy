package com.study.libclink.core;

import java.io.Closeable;
import java.io.IOException;

public interface Sender extends Closeable {
    /**
     * 异步发送
     * @param args 封装的数据
     * @param listener 通过这个回调，通过onCompleted处理接收完成的args
     */
    boolean sendAsync(IoArgs args ,IoArgs.IoArgsEventListener listener) throws IOException;
}
