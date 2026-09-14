package org.example;

public class Recado {
    private final int id;
    private final String autor;
    private final String mensagem;

    public Recado(int id, String autor, String mensagem) {
        this.id = id;
        this.autor = autor;
        this.mensagem = mensagem;
    }

    public int getId() { return id; }
    public String getAutor() { return autor; }
    public String getMensagem() { return mensagem; }

    public String toJson() {
        return "{\"id\":" + id
                + ", \"autor\":\"" + escapar(autor) + "\""
                + ", \"mensagem\":\"" + escapar(mensagem) + "\"" +
                "}";
    }

    // metodo para retornar o texto recevido para o formato correto do JSON
    private String escapar(String texto) {
        // \\ representa \ no texto
        return  texto.replace("\\", "\\\\")
                // \" representa " no texto
                .replace("\"", "\\\"")
                // \r representa o inicio da linha
                .replace("\r", "")
                // representa a quebra de linha
                .replace("\n", "\\n");
    }
}
