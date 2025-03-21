/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package service;
import com.iakk.meresbackend.JsonUtil;
import com.iakk.meresbackend.ingatlan;
import org.springframework.stereotype.Service;
import java.io.IOException;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
/**
 *
 * @author mdrag
 */
public class ingatlanService {
    private List<ingatlan> ingatlanok;
    private final String filePath = Paths.get("src/main/resources/ingatlanok.json").toString();

    public ingatlanService() {
        try {
            ingatlanok = JsonUtil.readFromJson(filePath);
        } catch (IOException e) {
            ingatlanok = new ArrayList<>();
            System.err.println("Hiba a JSON beolvasásakor: " + e.getMessage());
        }
    }

    public List<ingatlan> getAll() {
        return ingatlanok;
    }

    public ingatlan add(ingatlan ingatlan) throws IOException {
        if (ingatlan.getHirdetesDatuma() == null) {
            ingatlan.setHirdetesDatuma(LocalDate.now());
        }
        ingatlan.setId(getNextId());
        ingatlanok.add(ingatlan);
        JsonUtil.writeToJson(filePath, ingatlanok);
        return ingatlan;
    }

    public boolean delete(Integer id) throws IOException {
        boolean removed = ingatlanok.removeIf(i -> i.getId().equals(id));
        if (removed) {
            JsonUtil.writeToJson(filePath, ingatlanok);
        }
        return removed;
    }

    private Integer getNextId() {
        return ingatlanok.stream().mapToInt(ingatlan::getId).max().orElse(0) + 1;
    }
}
