package com.zhongkk.avro;

import org.apache.avro.file.DataFileReader;
import org.apache.avro.file.DataFileWriter;
import org.apache.avro.io.DatumReader;
import org.apache.avro.io.DatumWriter;
import org.apache.avro.specific.SpecificDatumReader;
import org.apache.avro.specific.SpecificDatumWriter;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

/**
 * @author yechenglong
 * @create 2018/5/3 14:39
 **/
public class TestAvro {
    @Test
    public void testDemo(){
        Teacher teacher = new Teacher();
        teacher.setUsername("ycl");
        teacher.setAge(24);
        Teacher teacher1 = new Teacher("zy",27);
        Teacher teacher2 = new Teacher().newBuilder().setUsername("xyy").setAge(25).build();
        Teacher teacher3 = new Teacher().newBuilder(teacher2).setUsername("jyx").build();
        System.out.println(teacher);
        System.out.println(teacher1);
        System.out.println(teacher2);
        System.out.println(teacher3);

    }

    @Test
    public void testWrite() throws IOException {
        Teacher teacher = new Teacher();
        teacher.setUsername("ycl");
        teacher.setAge(24);
        Teacher teacher1 = new Teacher("zy",27);
        Teacher teacher2 = new Teacher().newBuilder().setUsername("xyy").setAge(25).build();
        DatumWriter<Teacher> datumWriter = new SpecificDatumWriter(Teacher.class);
        DataFileWriter<Teacher> dataFileWriter = new DataFileWriter<Teacher>(datumWriter);
        dataFileWriter.create(teacher.getSchema(),new File("teacher.txt"));
        dataFileWriter.append(teacher1);
        dataFileWriter.append(teacher2);
        dataFileWriter.close();

    }


    @Test
    public void testRead() throws IOException {
        DatumReader<Teacher> datumReader = new SpecificDatumReader<Teacher>(Teacher.class);
        DataFileReader<Teacher> dataFileReader = new DataFileReader(new File("teacher.txt"),datumReader);
        while(dataFileReader.hasNext()){
            System.out.println(dataFileReader.next());
        }

    }
}
