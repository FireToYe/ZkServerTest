package com.zhongkk.seriaizable;

import org.junit.Test;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

/**
 * @author yechenglong
 * @create 2018/5/3 10:45
 **/
public class TestDemo {

    @Test
    public void testWrite()throws Exception{
        ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream("1.txt"));
        Student student = new Student("ycl",24);
        oos.writeObject(student);
        oos.close();
    }

    @Test
    public void testRead()throws Exception{
        ObjectInputStream objectInputStream = new ObjectInputStream(new FileInputStream("1.txt"));
        Student student = (Student) objectInputStream.readObject();
        System.out.println(student);
    }
}
