package com.example.controller;

import com.example.model.AnalysisResult; // Importa AnalysisResult para o retorno do service
import com.example.model.WordCount; // Mantido para o List, mas será da análise do AnalysisResult

import javax.annotation.Resource;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.inject.Inject; // Necessário para injetar EJBs ou beans CDI
import javax.jms.ConnectionFactory;
import javax.jms.JMSContext; // Usar JMSContext é a forma moderna e recomendada
import javax.jms.Queue;
import javax.jms.TextMessage;

import java.io.Serializable;
import java.util.List;
import java.util.stream.Collectors; // Para converter Map<String, Long> para List<WordCount>

@ManagedBean
@ViewScoped
public class PhraseAnalyzerController implements Serializable {

    private static final long serialVersionUID = 1L;

    // PhraseService não será mais injetado aqui para chamada direta de analyzeAndPublishResult
    // porque a chamada agora será feita pelo MDB. O controller apenas envia a mensagem JMS.
    // @Inject
    // private PhraseService phraseService;

    // Recursos JMS configurados no WildFly para ENVIAR a frase
    @Resource(lookup = "java:/jms/TopazConnectionFactory")
    private ConnectionFactory connectionFactory;

    @Resource(lookup = "java:/jms/queue/PhraseQueue") // Fila para enviar a frase para análise
    private Queue phraseQueue;

    // Atributos para o input da frase e exibição de resultados
    private String inputPhrase;

    // NOTA IMPORTANTE: Estes atributos (distinctWordsCount, wordCounts) NÃO serão atualizados
    // imediatamente após o envio da mensagem JMS neste método. Para exibi-los, você precisaria
    // de um mecanismo assíncrono de retorno de resultado (e.g., outro ManagedBean que escuta
    // uma fila de resultados JMS via MDB e atualiza um ApplicationScoped Bean, ou WebSockets/polling).
    private int distinctWordsCount;
    private List<WordCount> wordCounts;


    /**
     * Envia a frase digitada para a fila JMS para ser analisada de forma assíncrona.
     * O processamento e a publicação do resultado serão feitos pelo backend (MDB).
     */
    public void analyze() {
        if (inputPhrase == null || inputPhrase.trim().isEmpty()) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_WARN, "Por favor, digite uma frase para análise.", null));
            return;
        }

        try (JMSContext context = connectionFactory.createContext()) { // <--- CAMBIO AQUÍ
            TextMessage message = context.createTextMessage(inputPhrase);
            context.createProducer().send(phraseQueue, message);

            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Frase enviada para análise. O resultado será processado em segundo plano.", null));

            // Limpa o campo de entrada después del envío exitoso
            this.inputPhrase = null;

            // Reinicializa los resultados en la interfaz para indicar que se disparó un nuevo análisis.
            // Serán rellenados por un mecanismo asíncrono posterior, no aquí.
            this.distinctWordsCount = 0;
            this.wordCounts = null;

        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erro ao enviar a frase para análise: " + e.getMessage(), null));
            e.printStackTrace();
        }
    }

    // Getters and Setters
    public String getInputPhrase() {
        return inputPhrase;
    }

    public void setInputPhrase(String inputPhrase) {
        this.inputPhrase = inputPhrase;
    }

    // Los getters para los resultados no serán rellenados directamente por este método 'analyze'.
    // Para mostrar el resultado, la interfaz gráfica necesitaría una lógica de "escucha" asíncrona
    // o un redireccionamiento a una página que muestre resultados históricos/recientes.
    public int getDistinctWordsCount() {
        return distinctWordsCount;
    }

    public void setDistinctWordsCount(int distinctWordsCount) {
        this.distinctWordsCount = distinctWordsCount;
    }

    public List<WordCount> getWordCounts() {
        return wordCounts;
    }

    public void setWordCounts(List<WordCount> wordCounts) {
        this.wordCounts = wordCounts;
    }

    // Opcional: Si quieres un mecanismo simple para "mostrar" el resultado
    // sin la complejidad de WebSockets/Polling, puedes tener un método que
    // buscaría el resultado de un ManagedBean @ApplicationScoped que recibe
    // los mensajes de la ResultQueue a través de un MDB.
    // Ejemplo (requiere más configuración en el backend):
    /*
    @Inject
    private ResultDisplayBean resultDisplayBean; // Un nuevo bean ApplicationScoped para mostrar resultados

    public void loadLatestResult() {
        if (resultDisplayBean != null && resultDisplayBean.getLatestAnalysisResult() != null) {
            AnalysisResult latestResult = resultDisplayBean.getLatestAnalysisResult();
            this.distinctWordsCount = latestResult.getDistinctWordsCount();
            this.wordCounts = latestResult.getWordOccurrences().entrySet().stream()
                .map(entry -> new WordCount(entry.getKey(), entry.getValue()))
                .collect(Collectors.toList());
        }
    }
    */
}