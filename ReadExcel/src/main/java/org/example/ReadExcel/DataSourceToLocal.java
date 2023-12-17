package org.example.ReadExcel;

import org.apache.commons.dbcp2.BasicDataSource;

import java.sql.Connection;
import java.sql.SQLException;

public class DataSourceToLocal {
    private static BasicDataSource ds = new BasicDataSource();
    static {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
        ds.setUrl("jdbc:mysql://localhost:3306/interview");
        ds.setUsername("root");
        ds.setPassword("letmein");
        ds.setMinIdle(5);
        ds.setMaxIdle(10);
        ds.setMaxOpenPreparedStatements(100);
    }
    public static Connection getConnection() throws SQLException {
        return ds.getConnection();
    }
    private DataSourceToLocal(){ }
}