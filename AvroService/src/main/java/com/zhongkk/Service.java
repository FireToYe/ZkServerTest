package com.zhongkk;

import com.zhongkk.service.rpc.AddService;
import com.zhongkk.service.rpc.AddServiceImpl;
import org.apache.avro.ipc.NettyServer;
import org.apache.avro.ipc.specific.SpecificResponder;

import java.net.InetSocketAddress;

/**
 * @author yechenglong
 * @create 2018/5/3 15:13
 **/
public class Service {
    public static void main(String[] args) {
        NettyServer server = new NettyServer(new SpecificResponder(AddService.class,new AddServiceImpl()),new InetSocketAddress(6666));

    }
}
