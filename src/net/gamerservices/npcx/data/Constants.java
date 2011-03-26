package net.gamerservices.npcx.data;

import java.io.File;
import net.gamerservices.npcx.PropertyHandler;
import org.bukkit.plugin.PluginDescriptionFile;

/**
 * Global and not confined to one file.
 * 
 * @author Nijikokun
 */
public class Constants {
    // Files and Directories
    public static PropertyHandler Configuration;
    public static String Plugin_Directory;

    // Plugin info
    public static PluginDescriptionFile info = null;

    // World
    public static String WORLD = "world";

    // Versioning
    public static boolean UPDATE = true;
    public static String VERSION;

    // Relational SQL Generics
    public static String SQL_Hostname = "localhost";
    public static String SQL_Port = "3306";
    public static String SQL_Username = "npcx";
    public static String SQL_Password = "p4ssw0rd!";
    public static String SQL_Database = "npcx";

    public static void load(String file) {
        Configuration = new PropertyHandler(Plugin_Directory + File.separator + file);

        // SQL
        SQL_Hostname = Configuration.getString("db-host", SQL_Hostname);
        SQL_Username = Configuration.getString("db-user", SQL_Username);
        SQL_Password = Configuration.getString("db-pass", SQL_Password);
        SQL_Database = Configuration.getString("db-name", SQL_Database);
        SQL_Port = Configuration.getString("db-port", SQL_Port);
        VERSION = Configuration.getString("version", info.getVersion());
        UPDATE = Configuration.getBoolean("update", UPDATE);
        WORLD = Configuration.getString("world", WORLD);
    }
}
