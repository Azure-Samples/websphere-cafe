package com.azuresql.sample;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class demonstrates how to connect to Azure SQL Database using Entra ID (formerly Azure AD) authentication
 * and execute SQL queries provided via environment variables.
 * 
 * It uses the DefaultAzureCredential from Azure Identity library, which tries various authentication methods
 * in sequence until one succeeds.
 * 
 * Reference documentation "Connect using ActiveDirectoryDefault authentication mode":
 * - https://learn.microsoft.com/sql/connect/jdbc/connecting-using-azure-active-directory-authentication?view=azuresqldb-current#connect-using-activedirectorydefault-authentication-mode
 */
public class AzureSqlQueryExecutor {
    private static final Logger LOGGER = LoggerFactory.getLogger(AzureSqlQueryExecutor.class);
    
    // Environment variable names
    private static final String ENV_SERVER_NAME = "SERVER_NAME";
    private static final String ENV_DATABASE_NAME = "DATABASE_NAME";
    private static final String ENV_SQL_QUERY = "SQL_QUERY";
    
    // Connection parameters - loaded from environment variables
    private static String server;
    private static String database;
    private static String query;
    
    public static void main(String[] args) {
        // Get connection parameters from environment variables
        server = System.getenv(ENV_SERVER_NAME);
        database = System.getenv(ENV_DATABASE_NAME);
        query = System.getenv(ENV_SQL_QUERY);
        
        // Validate server name
        if (server == null || server.isEmpty()) {
            LOGGER.error("Environment variable {} not set or empty", ENV_SERVER_NAME);
            return;
        } else {
            // Ensure server name has the full domain
            if (!server.contains(".database.windows.net")) {
                server = server + ".database.windows.net";
            }
            LOGGER.info("Using server: {}", server);
        }
        
        // Validate database name
        if (database == null || database.isEmpty()) {
            LOGGER.error("Environment variable {} not set or empty", ENV_DATABASE_NAME);
            return;
        } else {
            LOGGER.info("Using database: {}", database);
        }
        
        // Check if we have a predefined query or need to get it from user
        if (query != null && !query.isEmpty()) {
            LOGGER.info("Executing query from environment variable: {}", query);
            try {
                executeQuery(query);
            } catch (Exception e) {
                LOGGER.error("Error executing query: {}", e.getMessage(), e);
                System.out.println("Error: " + e.getMessage());
            }
        }
    }
    
    /**
     * Executes a SQL query against the Azure SQL Database using Entra ID authentication.
     * 
     * @param query The SQL query to execute
     * @throws SQLException If a database access error occurs
     */
    private static void executeQuery(String query) throws SQLException {
        LOGGER.info("Connecting to Azure SQL Database with Entra ID authentication");
        
        // Build the connection string
        String connectionUrl = String.format(
            "jdbc:sqlserver://%s:1433;database=%s;encrypt=true;trustServerCertificate=false;loginTimeout=30;",
            server, database);
        
        Properties connectionProperties = new Properties();
        
        // Configure for Microsoft Entra ID (formerly Azure Active Directory) authentication
        // Using ActiveDirectoryDefault which leverages the DefaultAzureCredential
        connectionProperties.setProperty("authentication", "ActiveDirectoryDefault");
        
        try (Connection connection = DriverManager.getConnection(connectionUrl, connectionProperties);
             Statement statement = connection.createStatement()) {
            
            LOGGER.info("Connection successful! Executing query: {}", query);
            
            // Check if the query is a SELECT query or other DML
            boolean isResultSet = statement.execute(query);
            
            if (isResultSet) {
                // For SELECT queries
                try (ResultSet resultSet = statement.getResultSet()) {
                    printResultSet(resultSet);
                }
            } else {
                // For INSERT, UPDATE, DELETE queries
                int updateCount = statement.getUpdateCount();
                System.out.println("Query executed successfully. Rows affected: " + updateCount);
            }
            
            LOGGER.info("Query executed successfully");
            
        } catch (SQLException e) {
            LOGGER.error("SQL Error: {}", e.getMessage(), e);
            throw e;
        } catch (Exception e) {
            LOGGER.error("Unexpected error: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to execute query", e);
        }
    }
    
    /**
     * Prints the result set in a tabular format.
     * 
     * @param resultSet The result set to print
     * @throws SQLException If a database access error occurs
     */
    private static void printResultSet(ResultSet resultSet) throws SQLException {
        ResultSetMetaData metaData = resultSet.getMetaData();
        int columnCount = metaData.getColumnCount();
        
        // Print column headers
        for (int i = 1; i <= columnCount; i++) {
            System.out.print(metaData.getColumnName(i));
            if (i < columnCount) {
                System.out.print("\t|\t");
            }
        }
        System.out.println("\n" + "-".repeat(80));
        
        // Print rows
        int rowCount = 0;
        while (resultSet.next()) {
            rowCount++;
            for (int i = 1; i <= columnCount; i++) {
                System.out.print(resultSet.getString(i));
                if (i < columnCount) {
                    System.out.print("\t|\t");
                }
            }
            System.out.println();
        }
        
        System.out.println("\nTotal rows: " + rowCount);
    }
}
