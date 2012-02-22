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

import android.os.StrictMode;

/**
 * @author pjv
 *
 */
public class StrictModeWrapper {

	   /* class initialization fails when this throws an exception */
	   static {
	       try {
	           Class clazz = Class.forName("android.os.StrictMode");
	           Class.forName("android.os.StrictMode$ThreadPolicy$Builder");
	           Class.forName("android.os.StrictMode$VmPolicy$Builder");
	       } catch (Exception ex) {
	           throw new RuntimeException(ex);
	       }
	   }

	   /* calling here forces class initialization */
	   public static void checkAvailable() {}

	   public static void setThreadPolicy() {
			StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
			.detectAll()
			.penaltyLog()
			//.penaltyDropBox()
			//.penaltyDialog()
			.build());
	   }

	   public static void setVmPolicy() {
			StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder()
			.detectAll()
			.penaltyLog()
			//.penaltyDropBox()
			.build());
	   }
	   
	   public static void allowThreadDiskWrites(){
		   StrictMode.allowThreadDiskWrites();
	   }
}
