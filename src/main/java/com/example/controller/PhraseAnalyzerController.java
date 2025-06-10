package com.example.controller;

import com.example.service.PhraseService;
import com.example.model.WordCount;
import javax.inject.Inject;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import java.util.List;

@ManagedBean
@ViewScoped
public class PhraseAnalyzerController {
    
    @Inject
    private PhraseService phraseService;
    
    private String inputPhrase;
    private int distinctWordsCount;
    private List<WordCount> wordCounts;
        private String errorMessage; // Novo campo para mensagens

    
 public void analyze() {
        if (inputPhrase == null || inputPhrase.trim().isEmpty()) {
            errorMessage = "Por favor, digite uma frase para analisar.";
            distinctWordsCount = 0;
            wordCounts = null;
            return;
        }
        
        errorMessage = null;
        wordCounts = phraseService.analyzePhrase(inputPhrase);
        System.out.println("WordCounts: " + wordCounts);
        distinctWordsCount = wordCounts.size();
    }

    public void limpar() {
        inputPhrase = null;
        distinctWordsCount = 0;
        wordCounts = null;
        errorMessage = null;
    }
    
    // Getters and Setters
    public String getInputPhrase() { return inputPhrase; }
    public void setInputPhrase(String inputPhrase) { this.inputPhrase = inputPhrase; }
    public int getDistinctWordsCount() { return distinctWordsCount; }
    public List<WordCount> getWordCounts() { return wordCounts; }
      // Adicione este getter
    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }
}