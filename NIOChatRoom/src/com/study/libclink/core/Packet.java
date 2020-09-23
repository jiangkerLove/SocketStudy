package com.study.libclink.core;

import java.io.Closeable;
import java.io.IOException;

public abstract class Packet implements Closeable {

    protected byte type;
    protected int length;

    public byte getType() {
        return type;
    }

    public int getLength() {
        return length;
    }

}
