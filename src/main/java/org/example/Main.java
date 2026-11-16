package org.example;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;

public class Main {
    public static void main(String[] args) throws Exception {
        RecadoController.testarConexao();

        // 0.0.0.0 aceita conexoes de qualquer ip da rede
        HttpServer servidor = HttpServer.create(
                new InetSocketAddress("0.0.0.0", 8080), 0);

        servidor.createContext("/api/recados", RecadoController::ouvirRecados);
        servidor.createContext("/", Main::carregarPagina);

        servidor.start();

        System.out.println("VAMO NESSA!!!! aberto em: http://localhost:8080\nCelulares podem acessar pelo IP da rede local na porta 8080");
    }

    public static void carregarPagina(HttpExchange exchange) throws IOException {
        if (!exchange.getRequestMethod().equals("GET")) {
            RecadoController.response(exchange, 405, "Metodo não permitido", "text/plain");
            return;
        }
        try (InputStream arquivo = Main.class.getResourceAsStream("/public/index.html")) {
            if (arquivo == null) {
                RecadoController.response(exchange, 404, "Pagina não econtrada", "text/plain");
                return;
            }
            byte[] pagina = arquivo.readAllBytes();
            exchange.getResponseHeaders().set("Content-Type", "text/html; charset=UTF-8");
            exchange.sendResponseHeaders(200, pagina.length);
            exchange.getResponseBody().write(pagina);
            exchange.close();
        }
    }
}