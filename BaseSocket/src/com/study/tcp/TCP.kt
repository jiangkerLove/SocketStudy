package com.study.tcp

/**
 *      What is TCP
 *      Transmission Control Protocol
 *      TCP是传输控制协议，是一种面向链接的、可靠的、基于字节流的传输层通信协议，由IETF的RFC 793定义
 *      与UDP一样完成第四层传输层所指定的功能和职责
 *
 *      TCP的机制
 *      三次握手、四次挥手
 *      具有校验机制、保证了数据传输的可靠性
 *      可以动态调整传输速率保证了数据传输的稳定性
 *
 *      TCP链接、传输流程
 *      首先要经过三次握手，才能进行数据传输，bytes
 *
 *      TCP可以做什么
 *      聊天消息传输、推送
 *      单人语音、视频聊天等
 *      几乎UDP能做的都能做，但需要考虑复杂性、性能问题
 *      限制：无法进行广播、多播、搜索等操作
 *
 *      TCP核心API
 *      socket():创建一个空的socket，还有一些重载可以直接链接
 *      bind()：绑定一个Socket到一个本地地址和端口上，具有独占性的，端口不可重复
 *      connect()：链接到远程套接字
 *      accept():接受一个新的链接
 *      write():把数据写入到Socket输出流
 *      read():从Socket输入流读取数据
 *
 *
 */