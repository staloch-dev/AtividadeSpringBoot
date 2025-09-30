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
        <H2>Endpoints Disponíveis: </H2>
            <ul>
                <li><a href="/api/cep"></a> - Buscar CEP </li>
                <li><a href="">/api/fato-gato</a> - Fatos Gatos </li>
                <li><a href="">/api/piadas</a> - Piadas </li>
                <li></li>
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
    @GetMapping("/cep/{cep}")          
    public String consultarCep(@PathVariable String sCep) {
        try {
            String url = "https://viacep.com.br/ws/" + sCep + "/json/";
            String sJsonResposta = fazerRequisicao(url);

            String logradouro = extrairValorJson(sJsonResposta, "logradouro");
            String bairro     = extrairValorJson(sJsonResposta, "bairro");
            String localidade = extrairValorJson(sJsonResposta, "localidade");
            String uf         = extrairValorJson(sJsonResposta, "uf");

            return String.format("""
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
}
