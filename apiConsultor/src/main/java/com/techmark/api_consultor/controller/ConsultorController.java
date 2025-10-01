package com.techmark.api_consultor.controller;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/api")
public class ConsultorController {
    // Vai ser responsável por guardar os contadores das estatísticas
    private static Map<String, Integer> contadores = new HashMap<>();
    
    private static List<String> historico = new ArrayList<>();

    static {
        contadores.put("cep", 0);
        contadores.put("fato-gato", 0);
        contadores.put("piada", 0);
    }

    @GetMapping("/")
    public String home() {
        return """
        <h1>Consultor APIs - Spring Boot </h1>
        <h2>Endpoints Disponíveis: </h2>
            <ul>
                <li><a href="/api/cep/01001000">/api/cep/{cep}</a> - Buscar CEP</li>
                <li><a href="/api/fato">/api/fato</a> - Fatos de Gatos</li>
                <li><a href="/api/conselho">/api/conselho</a> - Conselhos Aleatórios</li>
            </ul>        
                """;
    }

    // Método responsável reutilizado do ConsultorApi original
    private String fazerRequisicao(String urlString) throws IOException {
        URL url = new URL(urlString);

        HttpURLConnection conexao = (HttpURLConnection) url.openConnection();
        
        conexao.setRequestMethod("GET");
        conexao.setRequestProperty("User-Agent", "Mozilla/5.0");
        
        BufferedReader leitor = new BufferedReader(
            new InputStreamReader(conexao.getInputStream())
        );

        StringBuilder resposta = new StringBuilder();

        String linha;

        while ((linha = leitor.readLine()) != null) {
            resposta.append(linha);
        }

        leitor.close();
        conexao.disconnect();

        return resposta.toString();
        
    }
    // Método para consultar CEP
    @GetMapping("/cep/{cep}")          
    public String consultarCep(@PathVariable String cep) {
        try {
            String url = "https://viacep.com.br/ws/" + cep + "/json/";
            String sJsonResposta = fazerRequisicao(url);

            String logradouro = extrairValorJson(sJsonResposta, "logradouro");
            String bairro     = extrairValorJson(sJsonResposta, "bairro");
            String localidade = extrairValorJson(sJsonResposta, "localidade");
            String uf         = extrairValorJson(sJsonResposta, "uf");

            return String.format("""
                Consulta do CEP -
                Logradouro: %s
                Bairro: %s          
                Localidade: %s
                UF: %s
                """, logradouro, bairro, localidade, uf);
        }
        catch (IOException erro) {
            return "Aconteceu um erro: " + erro.getMessage();
        }
    }

    private String extrairValorJson(String json, String chave) {
        try {
            String busca = "\"" + chave + "\":\"";
            int inicio = json.indexOf(busca);
            if (inicio == -1) {
                busca = "\"" + chave + "\":";
                inicio = json.indexOf(busca);
                if (inicio == -1) { 
                    return "Não existe esse campo!";
                }
                inicio += busca.length();
                int fim = json.indexOf(",", inicio);

                if (fim == -1) {
                    fim = json.indexOf("}", inicio);
                }

                return json.substring(inicio, fim).trim();
            }
            inicio += busca.length();
            int fim = json.indexOf("\"", inicio);
            return json.substring(inicio, fim).trim();

        } catch (Exception error) {
            return "Não encontado!";
        }
    }
    // Método para consultar fatos de gatos
    @GetMapping("/fato")          
    public String consultarFatosGatos() {
        try {
            String url = "https://catfact.ninja/fact";
            String sJsonResposta = fazerRequisicao(url);
    
            String fato = extrairValorJson(sJsonResposta, "fact");

            return String.format("""
                Consulta Fatos de Gatos -
                Fato: %s

                """, fato);
        }
        catch (IOException erro) {
            return "Aconteceu um erro: " + erro.getMessage();
        }
    }

    @GetMapping("/conselho")
    public String consultarConselhoAleatorio() {
        try {
            String url = "https://api.adviceslip.com/advice";
            String sJsonResposta = fazerRequisicao(url);

            String conselho = extrairValorJson(sJsonResposta, "advice");

            return String.format("""
                Consulta Conselho Aleatório -
                Conselho: %s

                """, conselho);
        }
        catch (IOException erro) {
            return "Aconteceu um erro: " + erro.getMessage();
        }
    }
}
