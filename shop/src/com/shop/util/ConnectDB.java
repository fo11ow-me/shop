package com.shop.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class ConnectDB {
	public static Connection connection = null;
	public final static  String drive = "com.mysql.cj.jdbc.Driver";
	public final static  String url = "jdbc:mysql://localhost:3306/shop?serverTimezone=GMT%2b8";
	public final static String name = "root";
	public final static String password = "123456";
	static {
		// 加载驱动
		try {
			Class.forName(drive);
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	// &allowPublicKeyRetrieval=true
	/**
	 * 
	 * @return:数据库连接对象
	 */
	//?serverTimezone=UTC
	public static Connection getConnection() {
		try {
			connection = DriverManager.getConnection(url,name, password);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return connection;
	}
	
	public static void closeAll(ResultSet resultSet,PreparedStatement preparedStatement,Connection connection) {
		if (resultSet != null) {
			try {
				resultSet.close();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		if (preparedStatement != null) {
			try {
				preparedStatement.close();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		if (connection != null) {
			try {
				connection.close();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
}
