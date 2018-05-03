package com.zhongkk.service.rpc;

import org.apache.avro.AvroRemoteException;

/**
 * @author yechenglong
 * @create 2018/5/3 15:11
 **/
public class AddServiceImpl implements AddService {
    public int add(int i, int j) throws AvroRemoteException {
            return i+j;
    }
}
