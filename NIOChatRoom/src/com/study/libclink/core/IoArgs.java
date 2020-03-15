package com.study.libclink.core;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

/**
 * IO输入和输出的参数类
 * 对ByteBuffer的封装，channel的read，write封装，并提供string接口
 */
public class IoArgs {
    private byte[] bytes = new byte[256];
    private ByteBuffer buffer = ByteBuffer.wrap(bytes);

    public int read(SocketChannel channel) throws IOException {
        buffer.clear();
        return channel.read(buffer);
    }

    public int write(SocketChannel channel) throws IOException {
        return channel.write(buffer);
    }

    public String bufferString() {
        //丢弃掉换行符
        return new String(bytes, 0, buffer.position() - 1);
    }

    public interface IoArgsEventListener {
        void onStarted(IoArgs args);

        void onCompleted(IoArgs args);
    }
}
