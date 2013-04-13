package com.askcs.asksensedemo.database;

import com.askcs.asksensedemo.model.Setting;
import com.askcs.asksensedemo.model.State;
import com.j256.ormlite.android.apptools.OrmLiteConfigUtil;

public class DatabaseConfigUtil extends OrmLiteConfigUtil {
		
	public static final Class<?>[] classes = new Class[] {
		Setting.class,
        State.class
	};

	public static void main(String[] args) throws Exception {
		writeConfigFile("ormlite_config.txt", classes);
	}
}
