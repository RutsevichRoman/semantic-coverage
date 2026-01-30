package org.example.semanticcoverage.model;

import java.util.List;

/**
 * @param coverageScore 0..100
 * @param confidence    0..1
 * @param matches       top-k
 */
public record RequirementCoverage(String requirementId, String requirementText, int coverageScore, double confidence,
                                  List<Match> matches) {

}
