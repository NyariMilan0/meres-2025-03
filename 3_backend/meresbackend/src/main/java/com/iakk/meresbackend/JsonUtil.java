/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.iakk.meresbackend;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import com.iakk.meresbackend.ingatlan;
/**
 *
 * @author mdrag
 */
public class JsonUtil {
    private static final ObjectMapper mapper = new ObjectMapper();

    static {
        mapper.registerModule(new JavaTimeModule());
    }

    public static List<ingatlan> readFromJson(String filePath) throws IOException {
        String json = new String(Files.readAllBytes(Paths.get(filePath)));
        return mapper.readValue(json, new TypeReference<List<ingatlan>>() {});
    }

    public static void writeToJson(String filePath, List<ingatlan> ingatlanok) throws IOException {
        String json = mapper.writeValueAsString(ingatlanok);
        Files.write(Paths.get(filePath), json.getBytes());
    }
}
