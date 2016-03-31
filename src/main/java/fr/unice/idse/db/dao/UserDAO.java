package fr.unice.idse.db.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.unice.idse.db.dao.object.UserObject;

// TODO : Speak about the password thing

public class UserDAO extends DAO<UserObject> {
	private Logger logger = LoggerFactory.getLogger(UserDAO.class);

	public UserDAO() {
		this.conn = new Connexion().getConnection();
	}

	public UserDAO(Connection conn) {
		this.conn = conn;
	}
	
	public Connection getConnection() {
		return this.conn;
	}
	
	@Override
	public boolean create(UserObject obj) throws SQLException {
		try {
			if(find(obj.getPseudo()) != null) {
				logger.warn("This pseudos is already in use");
				return false;
			}
			String query = "INSERT INTO users (u_pseudo, u_email, u_password, u_statut) VALUES (?, ?, ?, ?)";
			PreparedStatement stmt = getConnection().prepareStatement(query, PreparedStatement.RETURN_GENERATED_KEYS);
			
			int count = 1;
			stmt.setString(count++, obj.getPseudo());
			stmt.setString(count++, obj.getEmail());
			stmt.setString(count++, obj.getPassword());
			stmt.setInt(count, obj.getStatus());
			
			stmt.execute();
			getConnection().commit();
			stmt.getGeneratedKeys().next();
			obj.setId(stmt.getGeneratedKeys().getInt("GENERATED_KEY"));
			return true;
		} catch (SQLException e) {
			logger.error(e.getMessage(), e.getSQLState());
			throw e;
		}

	}

	/**
	 * Delete an user folowing is pseudos
	 * @throws Exception 
	 */
	@Override
	public boolean delete(UserObject obj) throws SQLException {
		try {
			if (find(obj.getId()) == null) {
				logger.warn("The user id was not found in the database");
				return false;
			}
			String query = "DELETE FROM users WHERE u_id = ?";
			PreparedStatement stmt = getConnection().prepareStatement(query);
			
			stmt.setInt(1, obj.getId());
			
			stmt.execute();
			getConnection().commit();
			return true;
		} catch (SQLException e) {
			logger.error(e.getMessage(), e.getCause());
			throw e;
		}
	}

	/**
	 * Update the user data
	 */
	@Override
	public boolean update(UserObject obj) {
		try {
			if (find(obj.getId()) != null) {
				throw new Exception(
						"Impossible to update an non existing object");
			}

			String query = String
					.format("UPDATE users SET u_pseudo=%s, u_email=%s, u_password=%s, u_statut=%b, u_banned=%i WHERE u_id=%i,",
							obj.getPseudo(), obj.getEmail(), obj.getPassword(),
							obj.getStatus(), obj.isBanned(), obj.getId());
			Statement stmt = getConnection().createStatement();
			stmt.executeUpdate(query);
			return true;
		} catch (Exception e) {
			logger.warn(e.getMessage(), e.getCause());
			return false;
		}
	}

	@Override
	public UserObject find(int id) {
		UserObject value = new UserObject();
		try {
			String query = "SELECT u_pseudo, u_email, u_password, u_statut, u_banned FROM users WHERE u_id = ?";
			PreparedStatement stmt = this.getConnection().prepareStatement(query);
			stmt.setInt(1, id);
			ResultSet result = stmt.executeQuery();
			if (result.next())
				value = new UserObject(id, 
						result.getString("u_pseudo"),
						result.getString("u_email"),
						result.getString("u_password"),
						result.getInt("u_statut"),
						result.getBoolean("u_banned"));

		} catch (SQLException e) {
			logger.warn(e.getMessage(), e.getCause());
		}
		return value;
	}

	public UserObject find(String pseudo) {
		UserObject value = new UserObject();
		try {
			ResultSet result = getConnection().createStatement().executeQuery(
					String.format("SELECT u_id, u_email, u_password, u_statut, u_banned  FROM users WHERE u_pseudo = %s", pseudo));
			if (result.first())
				value = new UserObject(
						result.getInt("u_id"), 
						pseudo,
						result.getString("u_email"),
						result.getString("u_password"),
						result.getInt("u_statut"),
						result.getBoolean("u_banned"));

		} catch (SQLException e) {
			logger.warn(e.getMessage(), e.getCause());
		}
		return value;
	}

}
