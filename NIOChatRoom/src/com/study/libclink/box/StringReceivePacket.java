package com.study.libclink.box;

import com.study.libclink.core.ReceivePacket;

import java.io.IOException;

public class StringReceivePacket extends ReceivePacket {
    private byte[] buffer;
    private int position;

    public StringReceivePacket(int len) {
        buffer = new byte[len];
        length = len;
    }

    @Override
    public void save(byte[] bytes, int count) {
        System.arraycopy(bytes, 0, buffer, position, count);
        position += count;
    }

    public String string(){
        return new String(buffer);
    }

    @Override
    public void close() throws IOException {

    }
}
