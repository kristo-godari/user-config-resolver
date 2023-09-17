package org.godari.config.resolver.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.util.ResourceUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

public class TestUtils {

    public static String readFile(String fileName) throws IOException {
        File file = ResourceUtils.getFile("classpath:" + fileName);
        return Files.readString(file.toPath());
    }

    public static <T> T readFileIntoObject(String filename, Class<T> targetClass) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.readValue(readFile(filename), targetClass);
    }
}
