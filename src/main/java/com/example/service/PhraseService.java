// src/main/java/com/example/service/PhraseService.java
package com.example.service;

import com.example.model.AnalysisResult; // Importa a nova classe de resultado
import javax.enterprise.context.ApplicationScoped;
import javax.annotation.Resource; // Para @Resource
import javax.jms.ConnectionFactory; // Para JMS
import javax.jms.JMSContext; // Para JMS 2.0
import javax.jms.Queue; // Para JMS
import javax.jms.ObjectMessage; // Para enviar objetos serializáveis

import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@ApplicationScoped // CDI Bean - Singleton no escopo da aplicação
public class PhraseService {

    // Injeta a ConnectionFactory configurada no WildFly
    @Resource(lookup = "java:/jms/TopazConnectionFactory")
    private ConnectionFactory connectionFactory;

    // Injeta a fila de resultados JMS configurada no WildFly
    @Resource(lookup = "java:/jms/queue/ResultQueue")
    private Queue resultQueue;

    /**
     * Analisa a frase fornecida e envia o resultado da análise para uma fila JMS.
     * Este método é o "motor de análise" e processará uma requisição por vez se
     * o MDB que o invoca estiver configurado com maxSession=1 ou se houver um
     * mecanismo de sincronização externo garantindo isso.
     *
     * @param phrase A frase a ser analisada.
     * @return O objeto AnalysisResult contendo a contagem de palavras distintas e as ocorrências.
     */
    public AnalysisResult analyzeAndPublishResult(String phrase) {
        if (phrase == null || phrase.trim().isEmpty()) {
            // Se a frase for vazia, retorna um resultado vazio e não envia mensagem JMS
            return new AnalysisResult(0, Collections.emptyMap());
        }

        // 1. Normalizar a frase: remover pontuação e converter para minúsculas
        String normalizedPhrase = phrase.toLowerCase()
                                        .replaceAll("[^a-zA-ZáàãéèíìóòõúùçÁÀÃÉÈÍÌÓÒÕÚÙÇ\\s]", "");

        // 2. Dividir a frase em palavras
        String[] words = normalizedPhrase.split("\\s+");

        // 3. Filtrar palavras vazias (resultantes de múltiplos espaços)
        words = Arrays.stream(words)
                      .filter(word -> !word.isEmpty())
                      .toArray(String[]::new);

        // 4. Calcular a quantidade de palavras distintas
        int distinctWordsCount = (int) Arrays.stream(words).distinct().count();

        // 5. Calcular a quantidade de ocorrências de cada palavra
        Map<String, Long> wordOccurrences = Arrays.stream(words)
            .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));

        // Criar o objeto de resultado
        AnalysisResult analysisResult = new AnalysisResult(distinctWordsCount, wordOccurrences);

        // 6. Enviar o resultado para a fila JMS
        try (JMSContext context = connectionFactory.createContext()) {
            // Cria uma ObjectMessage, pois AnalysisResult é Serializable
            ObjectMessage message = context.createObjectMessage(analysisResult);
            context.createProducer().send(resultQueue, message);
            System.out.println("Analysis result published to JMS queue: " + resultQueue.getQueueName());
        } catch (Exception e) {
            System.err.println("Error publishing analysis result to JMS: " + e.getMessage());
            e.printStackTrace();
            // Em uma aplicação real, você pode querer lançar uma exceção ou lidar com o erro de outra forma
        }

        return analysisResult; // Retorna o resultado para quem chamou (e.g., o MDB)
    }
}