package com.example.pdrdemo;

/**
 * Created by zbs on 2023.
 *  Modified by zbs 2023
 */
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;

public class DbConnectActivity {

//    private static Connection conn;
//    public static String ip = "192.168.0.10";
//    public static String port = "5432";
//    public static String database = "test";
//    public static String user = "postgres";
//    public static String pw = "123456";

    private static Connection conn;
    public static String ip = "127.0.0.1";
    public static String port = "5432";
    public static String database = "GEI";
    public static String user = "postgres";
    public static String pw = "123456";

//    private static final String URL = "jdbc:postgresql://" + "ip地址" + ":" + "端口号" + "/" + "数据库" + "?characterEncoding=utf8";
    private static final String URL = "jdbc:postgresql://" + ip + ":" + port + "/" + database;


    public static void connectDataBase() {
        try {
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException e1) {
            e1.printStackTrace();
        }
        try {
            conn = DriverManager.getConnection("jdbc:postgresql://" + ip + ":"+port+"/" + database, user, pw);
            conn.setAutoCommit(false);
            System.out.println("成功加载数据库驱动！" + URL);

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static Connection openConnection()
            throws SQLException {
        Connection conn = null;
        final String DRIVER_NAME = "org.postgresql.Driver";


        try {
            Class.forName(DRIVER_NAME);
            conn = DriverManager.getConnection(URL, user, pw);
            System.out.println("成功加载SQL驱动！" + URL);
        } catch (Exception e) {
            System.out.println(e.toString());
            e.printStackTrace();
        }
        return conn;
    }




    private static void closeDataBase() {
        if (conn != null) {
            try {
                conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
            conn = null;
        }
    }


    private static void connect() {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    connectDataBase();
                } catch (Exception e) {
                    System.out.print(e.getMessage());
                    e.printStackTrace();
                }
            }
        });
        thread.start();
        try {
            thread.join();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }




}
