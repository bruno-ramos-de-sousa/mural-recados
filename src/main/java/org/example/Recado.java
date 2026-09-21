package org.example;

public class Recado {
    private final int id;
    private final String autor;
    private final String message;

    public Recado(int id, String autor, String mensagem) {
        this.id = id;
        this.autor = autor;
        this.message = mensagem;
    }

    public int getId() { return id; }
    public String getAutor() { return autor; }
    public String getMessage() { return message; }

    public String toJson() {
        return "{\"id\":" + id
                + ", \"autor\":\"" + escape(autor) + "\""
                + ", \"mensagem\":\"" + escape(message) + "\"" +
                "}";
    }

    // metodo para retornar o texto recevido para o formato correto do JSON
    private String escape(String text) {
        // \\ representa \ no texto
        return  text.replace("\\", "\\\\")
                // \" representa " no texto
                .replace("\"", "\\\"")
                // \r representa o inicio da linha
                .replace("\r", "")
                // representa a quebra de linha
                .replace("\n", "\\n");
    }
}
