package org.example;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class RecadoDAO {
    public void create(Recado recado) throws SQLException {
        String sql = "INSERT INTO recados (autor, mensagem) VALUES (?, ?);";

        try (Connection connection = Connetion.open();
             PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, recado.getAutor());
            pstmt.setString(2, recado.getMessage());
            pstmt.executeUpdate();
        }
    }

    public List<Recado> list() throws SQLException {
        String sql = "SELECT id, autor, mensagem FROM recados ORDER BY id DESC";
        List<Recado> recados = new ArrayList<>();

        try (Connection connection = Connetion.open();
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
