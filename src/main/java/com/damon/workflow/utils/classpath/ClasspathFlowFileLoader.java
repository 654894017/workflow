package com.damon.workflow.utils.classpath;

import com.damon.workflow.exception.ProcessException;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.JarURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarFile;

/**
 * 读取classpath 下 flow 文件夹中的所有文件内容
 */
public class ClasspathFlowFileLoader {

    /**
     * 批量加载 classpath 下 flow 文件夹中的所有文件内容
     */
    public List<String> loadFilesFromFlowFolder() {
        List<String> fileContents = new ArrayList<>();
        try {
            // 获取 flow 文件夹的资源 URL
            Enumeration<URL> resources = getClass().getClassLoader().getResources("flow");
            while (resources.hasMoreElements()) {
                URL resource = resources.nextElement();
                if (resource.getProtocol().equals("file")) {
                    // 处理文件系统中的 flow 文件夹
                    fileContents.addAll(loadFilesFromFileSystem(resource));
                } else if (resource.getProtocol().equals("jar")) {
                    // 处理 JAR 包中的 flow 文件夹
                    fileContents.addAll(loadFilesFromJar(resource));
                }
            }
            return fileContents;
        } catch (Exception e) {
            throw new ProcessException("load flow file failed", e);
        }
    }

    /**
     * 从文件系统中的 flow 文件夹加载所有文件内容
     */
    private List<String> loadFilesFromFileSystem(URL folderUrl) throws Exception {
        List<String> fileContents = new ArrayList<>();
        Path folderPath = Paths.get(folderUrl.toURI());
        Files.walk(folderPath).filter(path -> path.toString().endsWith(".yaml")) // 只加载 YAML 文件
                .forEach(path -> {
                    try {
                        String content = new String(Files.readAllBytes(path), "UTF-8");
                        fileContents.add(content);
                    } catch (Exception e) {
                        throw new ProcessException("load flow file failed:" + path, e);
                    }
                });
        return fileContents;
    }

    /**
     * 从 JAR 包中的 flow 文件夹加载所有文件内容
     */
    private List<String> loadFilesFromJar(URL jarUrl) throws Exception {
        List<String> fileContents = new ArrayList<>();
        JarURLConnection jarConnection = (JarURLConnection) jarUrl.openConnection();
        JarFile jarFile = jarConnection.getJarFile();
        jarFile.stream().filter(entry -> entry.getName().startsWith("flow/") && entry.getName().endsWith(".yaml"))
                .forEach(entry -> {
                    try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream(entry.getName())) {
                        if (inputStream != null) {
                            fileContents.add(readContentFromInputStream(inputStream));
                        }
                    } catch (Exception e) {
                        throw new ProcessException("load flow file from jar file failed:" + entry.getName(), e);
                    }
                });
        return fileContents;
    }

    /**
     * 从 InputStream 读取文件内容
     */
    private String readContentFromInputStream(InputStream inputStream) throws Exception {
        StringBuilder content = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
            String line;
            while ((line = reader.readLine()) != null) {
                content.append(line).append("\n");
            }
        }
        return content.toString();
    }
}