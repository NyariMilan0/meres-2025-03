/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Controller;
import com.iakk.meresbackend.ingatlan;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import service.ingatlanService;
/**
 *
 * @author mdrag
 */
@RestController
@RequestMapping("/api/ingatlan")
public class ingatlanController {
    @Autowired
    private ingatlanService service;

    @GetMapping
    public List<ingatlan> getAll() {
        return service.getAll();
    }

    @PostMapping
    public ResponseEntity<?> add(@RequestBody ingatlan ingatlan) {
        try {
            if (ingatlan.getKategoria() == null || ingatlan.getLeiras() == null ||
                ingatlan.getTehermentes() == null || ingatlan.getAr() == null) {
                return ResponseEntity.badRequest().body("Hiányos adatok");
            }
            ingatlan saved = service.add(ingatlan);
            return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("Id", saved.getId()));
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Hiba a mentés során");
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Integer id) {
        try {
            if (service.delete(id)) {
                return ResponseEntity.noContent().build();
            }
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Az ingatlan nem létezik");
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Hiba a törlés során");
        }
    }
}
