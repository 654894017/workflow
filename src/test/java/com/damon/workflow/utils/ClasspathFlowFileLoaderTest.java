package com.damon.workflow.utils;

import com.damon.workflow.utils.classpath.ClasspathFlowFileLoader;
import org.junit.jupiter.api.Test;

import java.util.List;


class ClasspathFlowFileLoaderTest {
    @Test
    public void test()  {
        ClasspathFlowFileLoader loader = new ClasspathFlowFileLoader();
        List<String> files = loader.loadFilesFromFlowFolder();
        files.forEach(fileContent -> {
            System.out.println("File Content:");
            System.out.println(fileContent);
        });
    }
}