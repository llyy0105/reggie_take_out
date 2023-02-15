package com.ly.test;

import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * @Description
 * @Author ly
 * @create 2023-02-10 19:48
 */
public class UploadFileTest {

    @Test
    void test1() {
        String fileName = "asdhjkasjfhas.jpg";
        String suffix = fileName.substring(fileName.lastIndexOf("."));
        System.out.println(suffix);
    }

}
