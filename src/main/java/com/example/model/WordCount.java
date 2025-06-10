// src/main/java/com/example/model/WordCount.java
package com.example.model;

import java.io.Serializable;

public class WordCount implements Serializable {
    private static final long serialVersionUID = 1L; // Sempre adicione serialVersionUID

    private String word;
    private Long count;

    public WordCount() {}

    public WordCount(String word, Long count) {
        this.word = word;
        this.count = count;
    }

    public String getWord() {
        return word;
    }

    public void setWord(String word) {
        this.word = word;
    }

    public Long getCount() {
        return count;
    }

    public void setCount(Long count) {
        this.count = count;
    }

    @Override
    public String toString() {
        return "WordCount{" +
               "word='" + word + '\'' +
               ", count=" + count +
               '}';
    }
}