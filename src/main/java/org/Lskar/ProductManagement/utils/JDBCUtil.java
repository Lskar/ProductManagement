package org.Lskar.ProductManagement.utils;

import java.io.FileReader;
import java.sql.*;
import java.util.Properties;

public class JDBCUtil {

    private static String url=null;
    private static String username=null;
    private static String password=null;

    static {
        try {
            Properties prop = new Properties();
            prop.load(new FileReader("src\\main\\resources\\driver.properties"));
            url = prop.getProperty("url");
            username = prop.getProperty("username");
            password = prop.getProperty("password");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(url,username,password);
    }

    public static void close(Connection conn, PreparedStatement ps, ResultSet rs) {
        try{
            if(rs!=null) rs.close();
            if(ps!=null) ps.close();
            if(conn!=null) conn.close();
        }
        catch(SQLException e){
            e.printStackTrace();
        }
    }
    public static ResultSet select(Connection conn,PreparedStatement ps,String sql,Object... objs){
        try {
//            ps=conn.prepareStatement(sql);
//            if(obj!=null){
//                ps.setObject(1,obj);
//            }
//            return ps.executeQuery();
            ps=conn.prepareStatement(sql);
            if(objs!=null){
                int i=1;
                for(Object obj:objs){
                    ps.setObject(i++, obj);
                }
            }
            return ps.executeQuery();

        } catch (Exception e) {
            throw new ErrorInQueryException("执行查询语句时出错！");
        }
    }

    public static int update(Connection conn,PreparedStatement ps,String sql,Object... objs){
        try{
            ps=conn.prepareStatement(sql);
            if(objs!=null){
                int i=1;
                for(Object obj:objs){
                    ps.setObject(i++, obj);
                }
            }
            return ps.executeUpdate();
        }
        catch(Exception e){
            throw new ErrorInUpdateException("执行更新语句时出错");
        }
    }
    public static ResultSet update2(Connection conn,PreparedStatement ps,String sql,boolean isActive,Object... objs){
        try{
            if(isActive){
                ps=conn.prepareStatement(sql,Statement.RETURN_GENERATED_KEYS);
            }
            if(objs!=null){
                int i=1;
                for(Object obj:objs){
                    ps.setObject(i++, obj);
                }
            }
            ps.executeUpdate();
            return ps.getGeneratedKeys();
        }
        catch(Exception e){
            throw new ErrorInUpdateException("执行更新语句时出错");
        }
    }

    public static void startTransaction(Connection conn)  {
        try {
            conn.setAutoCommit(false);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public static void commitTransaction(Connection conn) {

        try {
            conn.commit();
            conn.setAutoCommit(true);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

    }

    public static void rollbackTransaction(Connection conn) {
        try {
            conn.rollback();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }


}
