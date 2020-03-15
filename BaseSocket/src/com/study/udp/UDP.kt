package com.study.udp

/**
 * What is UDP
 * User Datagram Protocol
 *
 * 1是一种用户数据协议，又称用户数据报文协议
 * 面向数据报，链接不可靠，没有服务器端和客户端的区别，谁发送谁就是服务器端
 *
 * 2是一种简单的面向数据报的传输协议，正式规范未RFC 768
 *
 * 3用户数据协议，非链接协议
 *  是不需要经过链接，仅仅只需要发送，若你监听了对应端口，就可以收到消息
 *
 *
 *      为什么是不可靠的
 *      它一旦把应用程序的数据发送给网络层，就不会保留数据备份，
 *      只管发送和接收数据，不会对发送的数据进行缓存、备份、重发等
 *      UDP在IP数据报的头部仅仅加入了复用和数据校验字段
 *      发送端生产数据，接收端从网络中抓取数据，是无链接的，时机不对不能接收到消息
 *      结构简单、无校验、速度快、容易丢包、可广播
 *
 *
 *      一般用来做什么
 *      发送文件，DNS、TFTP、SNMP
 *      视频、音频、普通数据(无关紧要的数据，因为存在丢包的可能)
 *
 *
 *      UDP会在发送的信息前加上8个字节的长度数据
 *      0～15  Source Port 储存发送端的端口号 两个字节0～65535 端口号的有效范围是从0到65535。动态端口的范围是从1024到65535
 *      16～31 Destination Port 储存接收端的端口号
 *      32～47 Length 储存字节长度
 *      48～63 Header and Data Checksum 储存头部和校验字段
 *
 *      UDP包最大长度
 *      一共是16位，可以存储的最大数为2^16-1=64k-1=65536-1=65535 byte
 *      自身协议占用了8个字节，8k
 *      在IP层进行封装后IP包头占去20k
 *      最终65535-20-8=65507个字节 大约63M多
 *
 *
 *      核心API-DatagramSocket
 *      用于接收和发送UDP的类
 *      负责发送某一个UDP包，或者接收UDP包
 *      不同于TCP，UDP并没有合并到Socket API中，接收端和发送端没有区别
 *      DatagramSocket()创建简单实例，不指定端口和IP，若直接发送，则会是随机一个可用的端口，IP为本机IP
 *      DatagramSocket(int port)创建监听固定端口的实例，注意这里的端口只是监听的端口，也是socket端口，但并非发送到的端口
 *      DatagramSocket(int port，InetAddress LocalAddr)创建固定端口指定IP的实例，监听指定端口和指定ip，当ip地址不止一个时候有效
 *      receive(DatagramPacket d):接收
 *      send(DatagramPacket d):发送
 *      setSoTimeout(int timeout):设置超时，毫秒
 *      close():关闭、释放资源
 *
 *      核心API-DatagramPacket
 *      用于处理报文
 *      将byte数组、目标地址、目标端口等数据包装成报文或者将报文拆卸成byte数组
 *      是UDP的发送实体、也是接收实体
 *      DatagramPacket(byte[] buf,int offset,int length,InetAddress address,int port)
 *      - 前面3个参数指定buf的使用区间，后面两个参数指定目标机器地址与端口
 *      DatagramPacket(byte[] buf,int offset,int length,SocketAddress address)
 *      - SocketAddress = InetAddress + port
 *      setData(byte[] buf,int offset,int length)
 *      setData(byte[] buf)
 *      setLength(int length)
 *      getData()、getOffset()、getLength()
 *      setAddress(InetAddress iaddr)、setPort(int iport),当发送时这两个参数有效，但是在接收时，这两个参数是发送过来的信息，
 *      getAddress()、getPort()
 *      setSocketAddress(SocketAddress address)、getSocketAddress()
 */