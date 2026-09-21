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
        testConnection();

        // 0.0.0.0 aceita conexoes de qualquer ip da rede
        HttpServer server = HttpServer.create(
                new InetSocketAddress("0.0.0.0", 8080), 0);

        server.createContext("/api/recados", Main::listenRecados);
        server.createContext("/", Main::loadPage);

        server.start();

        System.out.println("VAMO NESSA!!!! aberto em: http://localhost:8080\nCelulares podem acessar pelo IP da rede local na porta 8080");
    }
    private static void testConnection() throws SQLException {
        try (Connection ignore = Connetion.open()) {
            System.out.println("Banco de dados conectado!!!");
        }
    }

    private static void listenRecados(HttpExchange exchange) throws IOException {
        // Permite que o app Android e outros clientes acessem nossa API
        exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().set("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
        exchange.getResponseHeaders().set("Access-Control-Allow-Headers", "Comtent-Type");

        try {
            if (exchange.getRequestMethod().equals("OPTIONS")) {
                exchange.sendResponseHeaders(204, -1);
                exchange.close();
            } else if (exchange.getResponseHeaders().equals("GET")) {
                list(exchange);
            } else if (exchange.getResponseHeaders().equals("POST")) {
                create(exchange);
            } else {
                exchange.getRequestHeaders().set("Allow", "GET, POST, OPTIONS");
                response(exchange, 405, "{\"erro\":\"Método não permitido\"}");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            response(exchange, 500, "{\"erro\":\"Erro ao cessar o banco\"}");
        }
    }
    
    private static void create(HttpExchange exchange) throws IOException, SQLException {
        //Map: guarda informacoes no formato chave e value, como um pequeno dicionario
        Map<String, String> data = readForm(exchange);
        String autor = data.getOrDefault("autor", "").trim();
        String message = data.getOrDefault("mensagem", "").trim();
        if (autor.isBlank() || message.isEmpty()) {
            response(exchange, 400, "{\"erro\":\"Preencha todos os campos\"}");
            return;
        }
        DAO.create(new Recado(0, autor, message));
        response(exchange, 201, "{\"mensagem\":\"Recado cadastrado\"");
    }
    
    private static void list(HttpExchange exchange) throws IOException, SQLException {
        List<Recado> recados = DAO.list();
        //StringBuilder: classe para constuir e alterar textos sem criar novas strings a cada mudanca
        StringBuilder json = new StringBuilder("[");

        for (Recado recado: recados) {
            json.append(recado.toJson());
            json.append(",");
        }
        json.append("]");
        response(exchange, 200, json.toString());
    }
    
    private static Map<String, String> readForm(HttpExchange exchange) throws IOException {
        String body = new String(
            exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8            
        );
        //HashMap: classe que implementa o Map
        //Map: List e ArrayList = HashMap
        Map<String, String> data = new HashMap<>();
        for (String field: body.split("&")) {
            String[] parts = field.split("=", 2);
            String name = URLDecoder.decode(parts[0], StandardCharsets.UTF_8);
            String value = parts.length == 2
                    ? URLDecoder.decode(parts[1], StandardCharsets.UTF_8): "";
            //put: adiciona uma chave e um value no Map
            data.put(name, value);
        }
        return data;
    }
    
    private static void loadPage(HttpExchange exchange) throws IOException {
        if (!exchange.getRequestMethod().equals("GET")) {
            response(exchange, 405, "Metodo não permitido", "text/plain");
            return;
        }
        try (InputStream file = Main.class.getResourceAsStream("/public/index.html")) {
            if (file == null) {
                response(exchange, 404, "Pagina não econtrada", "text/plain");
                return;
            }
            byte[] page = file.readAllBytes();
            exchange.getResponseHeaders().set("Context-type", "text/html: charset=UTF-8");
            exchange.sendResponseHeaders(200, page.length);
            exchange.getResponseBody().write(page);
            exchange.close();
        }
    }
    
    private static void response(HttpExchange exchange, int status, String content) throws IOException {
        response(exchange, status, content, "application/json");
    }

    private static void response(HttpExchange exchange, int status, String content, String type) throws IOException {
        byte[] responseContent = content.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Context-Type", type + "; charset=UTF-8");
        exchange.sendResponseHeaders(status, responseContent.length);
        exchange.getResponseBody().write(responseContent);
        exchange.close();
    }
}