/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.iakk.meresbackend;
import java.time.LocalDate;
/**
 *
 * @author mdrag
 */
public class ingatlan {
    private Integer id;
    private Integer kategoria;
    private String leiras;
    private LocalDate hirdetesDatuma;
    private Boolean tehermentes;
    private Long ar;
    private String kepUrl;

    // Getters és setters
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public Integer getKategoria() { return kategoria; }
    public void setKategoria(Integer kategoria) { this.kategoria = kategoria; }
    public String getLeiras() { return leiras; }
    public void setLeiras(String leiras) { this.leiras = leiras; }
    public LocalDate getHirdetesDatuma() { return hirdetesDatuma; }
    public void setHirdetesDatuma(LocalDate hirdetesDatuma) { this.hirdetesDatuma = hirdetesDatuma; }
    public Boolean getTehermentes() { return tehermentes; }
    public void setTehermentes(Boolean tehermentes) { this.tehermentes = tehermentes; }
    public Long getAr() { return ar; }
    public void setAr(Long ar) { this.ar = ar; }
    public String getKepUrl() { return kepUrl; }
    public void setKepUrl(String kepUrl) { this.kepUrl = kepUrl; }
}
