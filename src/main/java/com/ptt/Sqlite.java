package com.ptt;

import java.sql.SQLException;
import java.sql.Connection;
import java.sql.Statement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import java.util.List;
import java.util.ArrayList;

import org.sqlite.SQLiteConfig;
import org.sqlite.SQLiteDataSource;

/**
 * @Author James Chien
 * @Version 0.0.1
 *
 * Sqlite library
 * */
public class Sqlite {
    
    private final static class Config
    {
        public final static String JDBC = "jdbc:sqlite:ptt.db";
        public final static String TABLE = "PTT";
    }

    /**
     * get sqlite connection
     *
     * @throws SQLException while execute sqlite statement
     * @return sqlite connection
     * */
    public static Connection getConnection()
        throws SQLException
    {
        SQLiteConfig config = new SQLiteConfig();
        config.setSharedCache(true);
        config.enableRecursiveTriggers(true);
        SQLiteDataSource sds = new SQLiteDataSource(config);
        sds.setUrl(Config.JDBC);
        
        return sds.getConnection();
    }

    /**
     * create ptt table
     *
     * @param conn sqlite connection
     * 
     * @throws SQLException
     * */
    public final static void create(Connection conn)
        throws SQLException
    {
        String sql = String.format("create table if not exists %s (title varchar(100), pdate varchar(10), author varchar(50), rating smallint, href text primary key);", Config.TABLE);
        Statement stmt = conn.createStatement();
        stmt.executeUpdate(sql);
    }

    /**
     * drop table if it exits
     *
     * @param conn sqlite connection
     * 
     * @throws SQLException
     * */
    public final static void drop(Connection conn)
        throws SQLException
    {
        String sql = String.format("drop table if exists %s;", Config.TABLE);
        Statement stmt = conn.createStatement();
        stmt.executeUpdate(sql);
    }


    /**
     * get Insert preparedStatement
     * 
     * @param conn sqlite connection
     * 
     * @throws SQLException
     * */
    public static PreparedStatement getInsertPStmt(Connection conn)
        throws SQLException
    {
        String sql = String.format("insert into %s (title, pdate, author, rating, href) values (?,?,?,?,?);", Config.TABLE);
        return conn.prepareStatement(sql);
    }
    

    /**
     * internal job for executing sql usage
     *
     * @param conn sqlite connection
     * @param sql
     *
     * @throws SQLException
     * */
    public static ResultSet execStmt(Connection conn, String sql)
        throws SQLException
    {
        Statement stmt = conn.createStatement();
        return stmt.executeQuery(sql);
    }
   
    /**
     * @param conn sqlite connection
     * @param pstmt sql prepared statement
     * @param dataList
     *
     * @throws SQLException
     * */ 
    public static void execPStmt(Connection conn, PreparedStatement pstmt, List<Object> dataList)
        throws SQLException
    {
        for(int index = 0 ; index < dataList.size() ; index++)
        {
            Object v = dataList.get(index);
            if(v instanceof String) {
                pstmt.setString(index+1, String.valueOf(v));
            } else {
                pstmt.setInt(index+1, (Integer)v);
            }
        }
        pstmt.executeUpdate();
    }

}
