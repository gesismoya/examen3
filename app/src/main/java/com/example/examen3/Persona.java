package com.example.examen3;

import java.sql.Timestamp;

public class Persona {
    private String Descripcion;
    private String Periodista;
    private String Fecha;

    public Persona() {
    }

    public String getDescripcion() {
        return Descripcion;
    }

    public void setDescripcion(String descripcion) {
        Descripcion = descripcion;
    }

    public String getPeriodista() {
        return Periodista;
    }

    public void setPeriodista(String periodista) {
        Periodista = periodista;
    }

  /*  public String getFecha() {
        return Fecha;
    }

    public void setFecha(String fecha) {
        Fecha = fecha;
        }*/
}