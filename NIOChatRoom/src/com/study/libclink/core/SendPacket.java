package com.study.libclink.core;

public abstract class SendPacket extends Packet{

    protected boolean canceled;

    public abstract byte[] bytes();

    public boolean isCanceled(){
        return canceled;
    }
}
