package com.study.libclink.core;

import java.io.IOException;

/**
 * 主入口，用来实现IoProvider的配置，就是你写库时的入口API
 */
public class IoContext {

    private static IoContext INSTANCE;
    private final IoProvider ioProvider;

    private IoContext(IoProvider ioProvider){
        this.ioProvider = ioProvider;
    }

    public IoProvider getIoProvider(){
        return ioProvider;
    }

    public static IoContext get(){
        return INSTANCE;
    }

    public static StartedBoot setUp(){
        return new StartedBoot();
    }

    public static void close() throws IOException{
        if (INSTANCE != null) {
            INSTANCE.callClose();
        }
    }

    private void callClose() throws IOException {
        ioProvider.close();
    }

    /**
     * 启动引导
     */
    public static class StartedBoot {

        private IoProvider ioProvider;

        private StartedBoot(){}

        public StartedBoot ioProvider(IoProvider ioProvider){
            this.ioProvider = ioProvider;
            return this;
        }

        public IoContext start(){
            INSTANCE = new IoContext(ioProvider);
            return INSTANCE;
        }
    }
}
