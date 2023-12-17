package org.example.ReadExcel;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class Connect {
    public static void main(String[] args) {
        try {
            Connection con = DataSourceToLocal.getConnection();
            PreparedStatement p = null;
            ResultSet rs = null;
            try {
                // SQL command data stored in String datatype
                String sql = "select * from studentbasicinformation";
                p = con.prepareStatement(sql);
                rs = p.executeQuery();

                // Printing ID, name, email of customers
                // of the SQL command above
                System.out.println("id\t\tname\t\temail");

                // Condition check
                while (rs.next()) {
                    String name = rs.getString("studentname");
                    System.out.println(name);
                }
            }

            // Catch block to handle exception
            catch (SQLException e) {

                // Print exception pop-up on screen
                System.out.println(e);
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
