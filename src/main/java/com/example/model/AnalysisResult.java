// src/main/java/com/example/model/AnalysisResult.java
package com.example.model;

import java.io.Serializable;
import java.util.Map;

public class AnalysisResult implements Serializable {
    private static final long serialVersionUID = 1L; // Sempre adicione serialVersionUID

    private int distinctWordsCount;
    private Map<String, Long> wordOccurrences;

    public AnalysisResult() {}

    public AnalysisResult(int distinctWordsCount, Map<String, Long> wordOccurrences) {
        this.distinctWordsCount = distinctWordsCount;
        this.wordOccurrences = wordOccurrences;
    }

    public int getDistinctWordsCount() {
        return distinctWordsCount;
    }

    public void setDistinctWordsCount(int distinctWordsCount) {
        this.distinctWordsCount = distinctWordsCount;
    }

    public Map<String, Long> getWordOccurrences() {
        return wordOccurrences;
    }

    public void setWordOccurrences(Map<String, Long> wordOccurrences) {
        this.wordOccurrences = wordOccurrences;
    }

    @Override
    public String toString() {
        return "AnalysisResult{" +
               "distinctWordsCount=" + distinctWordsCount +
               ", wordOccurrences=" + wordOccurrences +
               '}';
    }
}