package com.example.service;

import com.example.model.WordCount;
import javax.enterprise.context.ApplicationScoped;
import java.util.*;
import java.util.stream.Collectors;

@ApplicationScoped
public class PhraseService {
    
    public List<WordCount> analyzePhrase(String phrase) {
        if (phrase == null || phrase.trim().isEmpty()) {
            return Collections.emptyList();
        }
        
        String[] words = phrase.split("\\s+");
        
        Map<String, Long> wordCountMap = Arrays.stream(words)
            .map(String::toLowerCase)
            .collect(Collectors.groupingBy(w -> w, Collectors.counting()));
            
        return wordCountMap.entrySet().stream()
            .map(entry -> new WordCount(entry.getKey(), entry.getValue()))
            .sorted(Comparator.comparing(WordCount::getWord))
            .collect(Collectors.toList());
    }
}