package com.study.libclink.core;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

/**
 * IO输入和输出的参数类
 * 对ByteBuffer的封装，channel的read，write封装，并提供string接口
 */
public class IoArgs {
    private int limit = 256;
    private byte[] bytes = new byte[256];
    private ByteBuffer buffer = ByteBuffer.wrap(bytes);

    /**
     * 从bytes读取
     *
     * @param bytes
     * @param offset
     * @return
     */
    public int readFrom(byte[] bytes, int offset) {
        int size = Math.min(bytes.length - offset, buffer.remaining());
        buffer.put(bytes, offset, size);
        return size;
    }

    public int writeTo(byte[] bytes, int offset) {
        int size = Math.min(bytes.length - offset, buffer.remaining());
        buffer.get(bytes, offset, size);
        return size;
    }

    /**
     * 从socketChannel中读取数据
     *
     * @param channel
     * @return
     * @throws IOException
     */
    public int readFrom(SocketChannel channel) throws IOException {
        startWriting();
        int bytesProduced = 0;
        while (buffer.hasRemaining()) {
            int len = channel.read(buffer);
            if (len < 0) {
                throw new IOException();
            }
            bytesProduced += len;
        }
        finishWriting();

        return bytesProduced;
    }

    public int writeTo(SocketChannel channel) throws IOException {
        int bytesProduced = 0;
        while (buffer.hasRemaining()) {
            int len = channel.write(buffer);
            if (len < 0) {
                throw new IOException();
            }
            bytesProduced += len;
        }
        return bytesProduced;
    }

    public String bufferString() {
        //丢弃掉换行符
        return new String(bytes, 0, buffer.position() - 1);
    }

    public void startWriting() {
        buffer.clear();
        buffer.limit(limit);
    }

    public void finishWriting() {
        buffer.flip();
    }

    public void setLimit(int limit) {
        this.limit = limit;
    }

    public int capacity() {
        return buffer.capacity();
    }

    public interface IoArgsEventListener {
        void onStarted(IoArgs args);

        void onCompleted(IoArgs args);
    }

    public void writeLength(int total) {
        buffer.putInt(total);
    }

    public int readLength() {
        return buffer.getInt();
    }
}
