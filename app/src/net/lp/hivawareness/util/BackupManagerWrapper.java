/*
 *      This file is part of Transdroid <http://www.transdroid.org>
 *      
 *      Transdroid is free software: you can redistribute it and/or modify
 *      it under the terms of the GNU General Public License as published by
 *      the Free Software Foundation, either version 3 of the License, or
 *      (at your option) any later version.
 *      
 *      Transdroid is distributed in the hope that it will be useful,
 *      but WITHOUT ANY WARRANTY; without even the implied warranty of
 *      MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *      GNU General Public License for more details.
 *      
 *      You should have received a copy of the GNU General Public License
 *      along with Transdroid.  If not, see <http://www.gnu.org/licenses/>.
 *      
 */
/* Taken from Transdroid. */
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

import android.app.backup.BackupManager;
import android.content.Context;

/**
 * Wrapper class that uses reflection to call the BackupManager's dataChanged
 * @author Eric Kok
 */
public class BackupManagerWrapper {
	// If this throws an exception, then we don't support BackupManager
    /* class initialization fails when this throws an exception */
	static {
		try {
			Class.forName("android.app.backup.BackupManager");
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

    /* calling here forces class initialization */
	public static void checkAvailable() {}

	public void dataChanged() {
		mInstance.dataChanged();
	}

	private BackupManager mInstance;

	public BackupManagerWrapper(Context ctx) {
		mInstance = new BackupManager(ctx);
	}
}
