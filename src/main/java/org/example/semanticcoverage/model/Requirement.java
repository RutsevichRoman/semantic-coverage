package org.example.semanticcoverage.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Requirement {
    private final String id;
    private final String text;
    private float[] embedding;

    public Requirement(String id, String text){
        this.id = id;
        this.text = text;
    }
}
