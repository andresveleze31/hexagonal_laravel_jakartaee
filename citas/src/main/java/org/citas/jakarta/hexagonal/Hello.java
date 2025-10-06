package org.citas.jakarta.hexagonal;

public class Hello {

    private String name;

    public Hello(String name) {
        this.name = name;
    }

    public String getHello(){
        return name;
    }
}
