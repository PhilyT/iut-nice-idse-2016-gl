package fr.unice.idse.db.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import fr.unice.idse.constante.Config;

public class Connexion {
	private static Connection connect = null;
	
	public static Connection getConnection(){
		if(connect == null){
			try {
				Class.forName("com.mysql.jdbc.Driver");
				connect = DriverManager.getConnection(Config.url, Config.user, Config.pass);
				connect.setAutoCommit(false);
			} catch (SQLException | ClassNotFoundException e) {
				e.printStackTrace();
			}
		}		
		return connect;	
	}

}
