package org.example.semanticcoverage;

import org.springframework.stereotype.Component;

@Component
public class RunConfig {
    public String repo;
    public String requirements;
    public String out;
    public int filesAmount;

    public void loadFromArgs(String[] args) {
        for (String arg : args) {
            if (!arg.startsWith("--")) continue;
            String[] kv = arg.substring(2).split("=", 2);
            if (kv.length != 2) continue;
            switch (kv[0]) {
                case "repo" -> repo = kv[1];
                case "requirements" -> requirements = kv[1];
                case "filesAmount" -> filesAmount = Integer.parseInt(kv[1]);
                case "out" -> out = kv[1];
            }
        }
        if (repo == null || requirements == null || out == null) {
            throw new IllegalArgumentException("Usage: --repo=... --requirements=... --out=...");
        }
    }
}
