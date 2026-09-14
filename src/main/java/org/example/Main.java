package org.example;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

public class Main {
    private static final RecadoDAO DAO = new RecadoDAO();
    public static void main(String[] args) throws Exception{
        testarConexao();

        // 0.0.0.0 aceita conexoes de qualquer ip da rede
        HttpServer server = HttpServer.create(
                new InetSocketAddress("0.0.0.0", 8080), 0);

        server.createContext("/api/recados", Main::atenderRecados);
        server.createContext("/", Main::abrirPagina);

        server.start();

        System.out.println("VAMO NESSA!!!! aberto em: http://localhost:8080\nCelulares podem acessar pelo IP da rede local ena porta 8080");
    }
    private static void testarConexao() throws SQLException {
        try (Connection ingore = Conexao.abrir()) {
            System.out.println("Banco de dados conectado!!!");
        }
    }

    private static void atenderRecados(HttpExchange exchange) throws IOException {
        // Permite que o app Android e outros clientes acessem nossa API
        exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().set("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
        exchange.getResponseHeaders().set("Access-Control-Allow-Headers", "Comtent-Type");

        try {
            if (exchange.getRequestMethod().equals("OPTIONS")) {
                exchange.sendResponseHeaders(204, -1);
                exchange.close();
            } else if (exchange.getResponseHeaders().equals("GET")) {
                listar(exchange);
            } else if (exchange.getResponseHeaders().equals("POST")) {
                cadastrar(exchange);
            } else {
                exchange.getRequestHeaders().set("Allow", "GET, POST, OPTIONS");
                responder(exchange, 405, "{\"erro\":\"Método não permitido\"}");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            responder(exchange, 500, "{\"erro\":\"Erro ao cessar o banco\"}");
        }
    }
    
    private static void cadastrar(HttpExchange exchange) throws IOException, SQLException {
        //Map: guarda informacoes no formato chave e valor, como um pequeno dicionario
        Map<String, String> dados = lerFormulario(exchange);
        String autor = dados.getOrDefault("autor", "").trim();
        String mensagem = dados.getOrDefault("mensagem", "").trim();
        if (autor.isBlank() || mensagem.isEmpty()) {
            responder(exchange, 400, "{\"erro\":\"Preencha todos os campos\"}");
            return;
        }
        DAO.cadastrar(new Recado(0, autor, mensagem));
        responder(exchange, 201, "{\"mensagem\":\"Recado cadastrado\"");
    }
    
    private static void listar(HttpExchange exchange) throws IOException, SQLException {
        List<Recado> recados = DAO.listar();
        //StringBuilder: classe para constuir e alterar textos sem criar novas strings a cada mudanca
        StringBuilder json = new StringBuilder("[");

        for (Recado recado: recados) {
            json.append(recado.toJson());
            json.append(",");
        }
        json.append("]");
        responder(exchange, 200, json.toString());
    }

    private static void responder(HttpExchange exchange, int codigo, String conteudo) {

    }
}