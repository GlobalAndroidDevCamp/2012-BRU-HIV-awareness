/*   
 * 	 Copyright (C) 2008-2009 pjv (and others, see About dialog)
 * 
 * 	 This file is part of Collectionista.
 *
 *   Collectionista is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   Collectionista is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with Collectionista.  If not, see <http://www.gnu.org/licenses/>.
 */

package net.lp.hivawareness.util;

import java.io.IOException;

import net.lp.hivawareness.v4.HIVAwarenessActivity;
import android.app.backup.BackupAgentHelper;
import android.app.backup.BackupDataInput;
import android.app.backup.SharedPreferencesBackupHelper;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.ParcelFileDescriptor;

/**
 * A Backup Agent (as to the API 8)
 * 
 * Data backup being working can be shown on an emulator-2.2 using these steps: http://developer.android.com/guide/topics/data/backup.html#Testing
 * 
 * @author pjv
 *
 */
public class BackupAgent extends BackupAgentHelper {

    // A key to uniquely identify the set of backup data for the shared preferences
    static final String PREFS_BACKUP_KEY = "prefs";
    // A key to uniquely identify the set of backup data for the versioncheck preferences
    static final String VC_PREFS_BACKUP_KEY = "veecheckPrefs";
    // A key to uniquely identify the set of backup data for the internal storage files
    static final String INTERNAL_STORAGE_BACKUP_KEY = "internal_storage";

	/* (non-Javadoc)
	 * @see android.app.backup.BackupAgent#onCreate()
	 */
	@Override
	public void onCreate() {
		super.onCreate();

	    // Allocate a backup agent helper for shared preferences and add it to the backup agent
		SharedPreferencesBackupHelper sharedPreferencesBackupHelper = 
			new SharedPreferencesBackupHelper(this.getApplicationContext(), new String[]{HIVAwarenessActivity.PREFS});
        addHelper(PREFS_BACKUP_KEY, sharedPreferencesBackupHelper);
	}

	@Override
	public void onRestore(BackupDataInput data, int appVersionCode,
	        ParcelFileDescriptor newState) throws IOException {
		super.onRestore(data, appVersionCode, newState);
	    
	    //Might be a new environment, so some stuff will have to be redone: the analytics custom values. Retrigger by reenabling first run preference value.
        SharedPreferences prefs = getSharedPreferences(HIVAwarenessActivity.PREFS, Context.MODE_PRIVATE);
        prefs.edit().putBoolean(AnalyticsUtils.FIRST_RUN_KEY, true).apply();
	}
}
