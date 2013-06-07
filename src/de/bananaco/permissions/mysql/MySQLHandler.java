package de.bananaco.permissions.mysql;

import de.bananaco.permissions.Packages;
import de.bananaco.permissions.handlers.Handler;
import de.bananaco.permissions.ppackage.PPackage;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class MySQLHandler {

    public final String PACKAGE_TABLE = "package_table";
    public final String DATA_TABLE = "data_table";

    private String user = "";
    private String database = "";
    private String password = "";
    private String port = "";
    private String hostname = "";
    // which tables to check and create if not exist
    private Handler.DBType packageType;
    private Handler.DBType databaseType;
    // the connection, which should be null unless something is enabled
    private Connection c = null;

    public void loadSettings(Packages plugin) {
        // and which ones to load in the first place too...
        packageType = plugin.packageType;
        databaseType = plugin.databaseType;
        if (packageType == Handler.DBType.MYSQL || databaseType == Handler.DBType.MYSQL) {
            // now load
            plugin.getConfig().set("mysql.user", user = plugin.getConfig().getString("mysql.user", "user"));
            plugin.getConfig().set("mysql.database", database = plugin.getConfig().getString("mysql.database", "database"));
            plugin.getConfig().set("mysql.password", password = plugin.getConfig().getString("mysql.password", "password"));
            plugin.getConfig().set("mysql.port", port = plugin.getConfig().getString("mysql.port", "port"));
            plugin.getConfig().set("mysql.hostname", hostname = plugin.getConfig().getString("mysql.hostname", "hostname"));
            // save changes
            plugin.saveConfig();
            // enable connection
            MySQL MySQL = new MySQL(hostname, port, database, user, password);
            c = MySQL.open();
            // handle mysql table creation if necessary
            if (packageType == Handler.DBType.MYSQL && !hasTable(PACKAGE_TABLE)) {
                if(!hasTable(PACKAGE_TABLE)) {
                    createPackageTable();
                }
            }
            if (databaseType == Handler.DBType.MYSQL && !hasTable(DATA_TABLE)) {
                if(!hasTable(DATA_TABLE)) {
                    createDatabaseTable();
                }
            }
        }
    }

     // table management stuff

    public void createPackageTable() {
        String query = "CREATE TABLE " + PACKAGE_TABLE + " (\n" +
                "         id INT NOT NULL AUTO_INCREMENT PRIMARY KEY,\n" +
                "         package VARCHAR(32),\n" +
                "         permission VARCHAR(32),\n" +
                "         cur_timestamp TIMESTAMP(8)\n" +
                "       );";
        try {
            Statement s = c.createStatement();
            s.execute(query);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void createDatabaseTable() {
        String query = "CREATE TABLE " + DATA_TABLE + " (\n" +
                "         id INT NOT NULL AUTO_INCREMENT PRIMARY KEY,\n" +
                "         player VARCHAR(32),\n" +
                "         world VARCHAR(32),\n" +
                "         package VARCHAR(32),\n" +
                "         cur_timestamp TIMESTAMP(8)\n" +
                "       );";
        try {
            Statement s = c.createStatement();
            s.execute(query);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public boolean hasTable(String table) {
        String query = "IF object_id('" + table + "', 'U') is not null\n" +
                "       PRINT 'true'\n" +
                "       ELSE\n" +
                "       PRINT 'false'";
        try {
            // TODO how do I actually do this? lol
            Statement s = c.createStatement();
            s.executeQuery(query);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    // PackageManager stuff

    public PPackage getPPackage(String p) {
        String query = "SELECT permission FROM " + PACKAGE_TABLE + " WHERE package='" + p + "'";
        List<String> permissions = new ArrayList<String>();
        try {
            Statement s = c.createStatement();
            ResultSet results = s.executeQuery(query);
            while(results.next()) {
                String perm = results.getString("permission");
                permissions.add(perm);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return PPackage.loadPackage(p, permissions);
    }

    // Database stuff

    public void insertEntry(String p, String permission) {
        // TODO fill in
    }

    public void removeEntry(String player, String world, String value) {
        // TODO fill in
    }

    public void addEntry(String player, String world, String value) {
        // TODO fill in
    }

    public List<String> getEntries(String player, String tag) {
        String query = "SELECT package FROM " + DATA_TABLE + " WHERE player='" + player + "' AND world='" + tag + "'";
        List<String> packages = new ArrayList<String>();
        try {
            Statement s = c.createStatement();
            ResultSet results = s.executeQuery(query);
            while(results.next()) {
                String pack = results.getString("package");
                packages.add(pack);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return packages;
    }
}
