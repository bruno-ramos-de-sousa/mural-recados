package org.example;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Main {
    private static final RecadoDAO DAO = new RecadoDAO();
    public static void main(String[] args) throws Exception{
        testarConexao();

        // 0.0.0.0 aceita conexoes de qualquer ip da rede
        HttpServer servidor = HttpServer.create(
                new InetSocketAddress("0.0.0.0", 8080), 0);

        servidor.createContext("/api/recados", Main::ouvirRecados);
        servidor.createContext("/", Main::carregarPagina);

        servidor.start();

        System.out.println("VAMO NESSA!!!! aberto em: http://localhost:8080\nCelulares podem acessar pelo IP da rede local na porta 8080");
    }
    private static void testarConexao() throws SQLException {
        try (Connection ignored = Conexao.abrir()) {
            System.out.println("Banco de dados conectado!!!");
        }
    }

    private static void ouvirRecados(HttpExchange exchange) throws IOException {
        // Permite que o app Android e outros clientes acessem nossa API
        exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().set("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
        exchange.getResponseHeaders().set("Access-Control-Allow-Headers", "Comtent-Type");

        try {
            if (exchange.getRequestMethod().equals("OPTIONS")) {
                exchange.sendResponseHeaders(204, -1);
                exchange.close();
            } else if (exchange.getRequestMethod().equals("GET")) {
                listar(exchange);
            } else if (exchange.getRequestMethod().equals("POST")) {
                cadastrar(exchange);
            } else if (exchange.getRequestMethod().equals("DELETE")) {
                deletar(exchange);
            } else {
                exchange.getRequestHeaders().set("Allow", "GET, POST, OPTIONS");
                response(exchange, 405, "{\"erro\":\"Método não permitido\"}");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            response(exchange, 500, "{\"erro\":\"Erro ao cessar o banco\"}");
        }
    }
    
    private static void cadastrar(HttpExchange exchange) throws IOException, SQLException {
        //Map: guarda informacoes no formato chave e value, como um pequeno dicionario
        Map<String, String> dados = lerFormulario(exchange);
        String autor = dados.getOrDefault("autor", "").trim();
        String mensagem = dados.getOrDefault("mensagem", "").trim();
        if (autor.isBlank() || mensagem.isEmpty()) {
            response(exchange, 400, "{\"erro\":\"Preencha todos os campos\"}");
            return;
        }
        DAO.cadastrar(new Recado(0, autor, mensagem));
        response(exchange, 201, "{\"mensagem\":\"Recado cadastrado\"}");
    }

    private static void deletar(HttpExchange exchange) throws SQLException, IOException {
        Map<String, String> dados = lerFormulario(exchange);
        int id = Integer.parseInt(dados.getOrDefault("id", "").trim());

        Recado recado = DAO.getRecadoById(id);
        DAO.deletar(recado);
        response(exchange, 204);
    }
    
    private static void listar(HttpExchange exchange) throws IOException, SQLException {
        List<Recado> recados = DAO.listar();
        //StringBuilder: classe para constuir e alterar textos sem criar novas strings a cada mudanca
        StringBuilder json = new StringBuilder("[");

        for (int i = 0; i < recados.size(); i++) {
            if (i > 0) json.append(",");
            json.append(recados.get(i).toJson());
        }
        json.append("]");
        response(exchange, 200, json.toString());
    }
    
    private static Map<String, String> lerFormulario(HttpExchange exchange) throws IOException {
        String body = new String(
            exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8            
        );
        //HashMap: classe que implementa o Map
        //Map: List e ArrayList = HashMap
        Map<String, String> dados = new HashMap<>();
        for (String field: body.split("&")) {
            String[] partes = field.split("=", 2);
            String nome = URLDecoder.decode(partes[0], StandardCharsets.UTF_8);
            String valor = partes.length == 2
                    ? URLDecoder.decode(partes[1], StandardCharsets.UTF_8): "";
            //put: adiciona uma chave e um valor no Map
            dados.put(nome, valor);
        }
        return dados;
    }
    
    private static void carregarPagina(HttpExchange exchange) throws IOException {
        if (!exchange.getRequestMethod().equals("GET")) {
            response(exchange, 405, "Metodo não permitido", "text/plain");
            return;
        }
        try (InputStream arquivo = Main.class.getResourceAsStream("/public/index.html")) {
            if (arquivo == null) {
                response(exchange, 404, "Pagina não econtrada", "text/plain");
                return;
            }
            byte[] pagina = arquivo.readAllBytes();
            exchange.getResponseHeaders().set("Content-Type", "text/html; charset=UTF-8");
            exchange.sendResponseHeaders(200, pagina.length);
            exchange.getResponseBody().write(pagina);
            exchange.close();
        }
    }
    
    private static void response(HttpExchange exchange, int status, String content) throws IOException {
        response(exchange, status, content, "application/json");
    }

    private static void response(HttpExchange exchange, int status, String content, String type) throws IOException {
        byte[] responseContent = content.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", type + "; charset=UTF-8");
        exchange.sendResponseHeaders(status, responseContent.length);
        exchange.getResponseBody().write(responseContent);
        exchange.close();
    }

    private static void response(HttpExchange exchange, int status) throws IOException {
        response(exchange, status);
    }
}