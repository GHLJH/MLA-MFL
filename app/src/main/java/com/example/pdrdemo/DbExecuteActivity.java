package com.example.pdrdemo;
/**
 * Created by zbs on 2023.
 *  Modified by zbs 2023
 *   * 移动地理空间大数据云服务创新团队 www.dxkjs.com/
 */
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class DbExecuteActivity {

    public static boolean transferDate(String StrSQLcmd) throws SQLException {

        Connection conn = DbConnectActivity.openConnection();
        if (conn == null) {
            return false;
        }
        Statement statement = null;
        boolean result = false;
        try {
            statement = conn.createStatement();
            if (statement != null) {
                String cmd1 = "sql语句";
                ResultSet resultSet=statement.executeQuery(StrSQLcmd);
                result = statement.execute(StrSQLcmd);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            if (statement != null) {
                statement.close();
                statement = null;
            }
            return result;
        }
    }



    public static void connect(final String sqlcmd) {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    transferDate(sqlcmd);
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
