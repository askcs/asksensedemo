package com.askcs.asksensedemo.database;

import com.askcs.asksensedemo.model.Setting;
import com.j256.ormlite.android.apptools.OrmLiteConfigUtil;

public class DatabaseConfigUtil extends OrmLiteConfigUtil {
		
	public static final Class<?>[] classes = new Class[] {
		Setting.class,
	};

	public static void main(String[] args) throws Exception {
		writeConfigFile("ormlite_config.txt", classes);
	}
}
