package com.askcs.asksensedemo.database;

import com.askcs.asksensedemo.model.Setting;
import com.askcs.asksensedemo.model.State;
import com.j256.ormlite.android.apptools.OrmLiteConfigUtil;
import java.io.IOException;
import java.sql.SQLException;

/**
 * A utility class to create ORMLite config files.
 */
public class DatabaseConfigUtil extends OrmLiteConfigUtil {

    /**
     * The name of the generated ORMLite config file.
     */
    public static final String CONFIG_FILE_NAME = "ormlite_config.txt";

    /**
     * An array cof Class-es which will be stored in the local DB.
     */
    public static final Class<?>[] CLASSES = new Class[]{
            Setting.class,
            State.class
    };

    /**
     * A main method that needs to be executed when a new model class is
     * introduced to the code base.
     *
     * @param args command line parameters (which are ignored).
     *
     * @throws IOException  when the config file cannot be written to `res/raw/`.
     * @throws SQLException when one of the Class-es in `CLASSES` contains invalid
     *                      SQL annotations.
     */
    public static void main(String[] args) throws IOException, SQLException {
        writeConfigFile(CONFIG_FILE_NAME, CLASSES);
    }
}
