package org.example;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class RecadoDAO {
    public void cadastrar(Recado recado) throws SQLException {
        String sql = "INSERT INTO recado (autor, mensagem) VALUES (?, ?);";

        try (Connection connection = Conexao.abrir();
             PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, recado.getAutor());
            pstmt.setString(2, recado.getMensagem());
            pstmt.executeUpdate();
        }
    }

    public Recado getRecadoById(int id) throws SQLException {
        String sql = "SELECT id, autor, mensagem FROM recado WHERE id = ?";

        try (Connection connection = Conexao.abrir();
            PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    int idRecado = rs.getInt("id");
                    String autor = rs.getString("autor");
                    String mensagem = rs.getString("mensagem");

                    Recado recado = new Recado(idRecado, autor, mensagem);

                }
                return recado;
            }
        }

    }

    public void deletar(Recado recado) throws SQLException {
        String sql = "DELETE FROM recado WHERE id = ?";

        try (Connection connection = Conexao.abrir();
             PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, recado.getId());
            pstmt.executeQuery();
        }
    }

    public List<Recado> listar() throws SQLException {
        String sql = "SELECT id, autor, mensagem FROM recado ORDER BY id DESC";
        List<Recado> recados = new ArrayList<>();

        try (Connection connection = Conexao.abrir();
             PreparedStatement pstmt = connection.prepareStatement(sql);
             ResultSet resultado = pstmt.executeQuery()) {

            while (resultado.next()) {
                Recado recado = new Recado(
                        resultado.getInt("id"),
                        resultado.getString("autor"),
                        resultado.getString("mensagem")
                );
                recados.add(recado);
            }
        }
        return recados;
    }
}
