package com.zhongkk;

import com.zhongkk.service.rpc.AddService;
import org.apache.avro.ipc.NettyTransceiver;
import org.apache.avro.ipc.specific.SpecificRequestor;

import java.io.IOException;
import java.net.InetSocketAddress;

/**
 * @author yechenglong
 * @create 2018/5/3 15:17
 **/
public class Client {
    public static void main(String[] args) throws IOException {
        NettyTransceiver nettyTransceiver = new NettyTransceiver(new InetSocketAddress("127.0.0.1",6666));
        AddService service = SpecificRequestor.getClient(AddService.class,nettyTransceiver);
        System.out.println(service.add(1,2));
    }
}
