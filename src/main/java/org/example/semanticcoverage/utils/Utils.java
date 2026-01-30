package org.example.semanticcoverage.utils;

import java.nio.file.Path;

import org.example.semanticcoverage.model.TestCaseDoc;

public class Utils {

    public static Path getTestFileName(Path cacheTestDir, TestCaseDoc testDoc) {
        return cacheTestDir.resolve(testDoc.methodName + ".json");
    }
}
