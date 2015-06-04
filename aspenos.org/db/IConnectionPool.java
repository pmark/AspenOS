package org.aspenos.db;

import java.sql.*;
import org.aspenos.exception.*;


public interface IConnectionPool {

	public DbPersistence getConnection() throws SQLException;

	public void returnConnection(DbPersistence returned);

}
