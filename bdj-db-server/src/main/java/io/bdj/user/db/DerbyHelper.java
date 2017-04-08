package io.bdj.user.db;

import java.sql.SQLException;

/**
 *
 */
public final class DerbyHelper {

    private DerbyHelper() {}
    public static boolean tableAlreadyExists(final SQLException e) {

        boolean exists;
        if("X0Y32".equals(e.getSQLState()) && e.getMessage().startsWith("Table/View")) {
            exists = true;
        } else {
            exists = false;
        }
        return exists;
    }
    public static boolean schemaAlreadyExists(final SQLException e) {

        boolean exists;
        if("X0Y68".equals(e.getSQLState()) && e.getMessage().startsWith("Schema")) {
            exists = true;
        } else {
            exists = false;
        }
        return exists;
    }
}
