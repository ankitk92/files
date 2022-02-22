package com.cvent.soap.utility;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.handlers.MapListHandler;
import org.apache.commons.dbutils.handlers.ScalarHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Util class to make DB calls
 */
public class DBUtils {
    private static final Logger LOGGER = LoggerFactory.getLogger(DBUtils.class);
    private static final String QUERY_ERROR = "Error in running the query";
    private static final HikariConfig HIKARI_CONFIG = new HikariConfig();
    private final HikariDataSource dataSource;

    public DBUtils(Map<String, Object> config) {
        HIKARI_CONFIG.setJdbcUrl(config.get("url").toString());
        HIKARI_CONFIG.setUsername((String) config.get("username"));
        HIKARI_CONFIG.setPassword((String) config.get("password"));
        HIKARI_CONFIG.setDriverClassName((String) config.get("driverClassName"));
        dataSource = new HikariDataSource(HIKARI_CONFIG);
    }

    public DBUtils(HikariConfig config) {
        dataSource = new HikariDataSource(config);
    }

    /**
     * execute the query and return a single column value as an Object
     *
     * @param query - query String
     * @return - return value
     */
    public Object readValue(String query) {
        try (Connection connection = dataSource.getConnection()) {
            query = query.replace("\n", " ").replaceAll("\\s{2,}", " ").trim();
            return new QueryRunner().query(connection, query, new ScalarHandler<>());
        } catch (SQLException exp) {
            LOGGER.error(QUERY_ERROR + query, exp);
        }
        return null;
    }

    /**
     * execute the query and return the output in Map of column and value for a single row result
     * @param query - query
     * @return - Map result (column, value)
     */
    public Map<String, Object> readRow(String query) {
        try (Connection connection = dataSource.getConnection()) {
            query = query.replace("\n", " ").replaceAll("\\s{2,}", " ").trim();
            return (new QueryRunner().query(connection, query, new MapListHandler())).get(0);
        } catch (SQLException exp) {
            LOGGER.error(QUERY_ERROR + query, exp);
        }
        return null;
    }

    /**
     * execute the query and return the output in List of Map of column and value for a multi row result
     * @param query - query String
     * @return - List of Map (column, value)
     */
    public List<Map<String, Object>> readRows(String query) {
        try (Connection connection = dataSource.getConnection()) {
            query = query.replace("\n", " ").replaceAll("\\s{2,}", " ").trim();
            return new QueryRunner().query(connection, query, new MapListHandler());
        } catch (SQLException exp) {
            LOGGER.error(QUERY_ERROR + query, exp);
        }
        return new ArrayList<>();
    }
    public void runQueryWithoutResult(String sp) {
        try (Connection conn = dataSource.getConnection();
             CallableStatement callableStatement = conn.prepareCall(sp)) {
            callableStatement.execute();
        } catch (SQLException exp) {
            LOGGER.error(QUERY_ERROR + sp, exp);
        };
    }
}
