package com.zhongkk.seriaizable;

import java.io.Serializable;

/**
 * @author yechenglong
 * @create 2018/5/3 10:44
 **/
public class Student implements Serializable{

    private static final long serialVersionUID = -5366914998750334596L;
    private String username;
    private int age;
    private String job;

    public Student(String username, int age) {
        this.username = username;
        this.age = age;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public String getJob() {
        return job;
    }

    public void setJob(String job) {
        this.job = job;
    }

    @Override
    public String toString() {
        return "Student{" +
                "username='" + username + '\'' +
                ", age=" + age +
                ", job='" + job + '\'' +
                '}';
    }
}
