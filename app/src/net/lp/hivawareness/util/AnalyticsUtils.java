/*
 * Copyright 2011 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
/*From iosched by Google.*/
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

import java.util.concurrent.RejectedExecutionException;

import net.lp.hivawareness.v4.HIVAwarenessActivity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Build;
import android.preference.PreferenceManager;
import android.util.Log;

import com.flurry.android.FlurryAgent;
import com.google.android.apps.analytics.GoogleAnalyticsTracker;

/**
 * Helper singleton class for the Google Analytics tracking library.
 */
public class AnalyticsUtils {
    private static final String TAG = HIVAwarenessActivity.TAG + "AnalyticsUtils";

    GoogleAnalyticsTracker mTracker;

	private Context mApplicationContext;

    public static final String FIRST_RUN_KEY = "analyticsFirstRun";
    private static final boolean ANALYTICS_ENABLED = true;
	
	private static final int GA_SCOPE_VISITOR = 1;
	private static final int GA_SCOPE_SESSION = 2;
	private static final int GA_SCOPE_PAGE = 3;
	
	private static final int GA_INDEX_VISITOR_APP_VERSION = 1;
	private static final String GA_KEY_APP_VERSION = "app version";
	
	private static final int GA_INDEX_VISITOR_APP_FORMAT = 2;
	private static final String GA_KEY_APP_FORMAT = "app format";
	
	private static final int GA_INDEX_VISITOR_PHONE = 3;
	private static final String GA_KEY_PHONE = "phone";
	
    private static AnalyticsUtils sInstance;

    /**
     * Returns the global {@link AnalyticsUtils} singleton object, creating one if necessary.
     */
	@SuppressWarnings("unused")
    public static AnalyticsUtils getInstance() {
        if (!ANALYTICS_ENABLED) {
            return sEmptyAnalyticsUtils;
        }
        
        final Context context = HIVAwarenessActivity.getAppCtxt();

        if (sInstance == null) {
            if (context == null) {
                return sEmptyAnalyticsUtils;
            }
            sInstance = new AnalyticsUtils(context);
        }

        return sInstance;
    }

    private AnalyticsUtils(Context context) {
        if (context == null) {
            // This should only occur for the empty Analytics utils object.
            return;
        }

        mApplicationContext = context/*.getApplicationContext()*/;
        mTracker = GoogleAnalyticsTracker.getInstance();

        // Unfortunately this needs to be synchronous.
        mTracker.start(HIVAwarenessActivity.GOOGLE_ANALYTICS_WEB_PROPERTY_ID, HIVAwarenessActivity.DEBUG?1:600, mApplicationContext);
        mTracker.setDebug(HIVAwarenessActivity.DEBUG);
        mTracker.setDryRun(HIVAwarenessActivity.DEBUG);

        Log.d(TAG, "Initializing Analytics");
        
        //Copy over old preferences configuration.
		SharedPreferences prefs = mApplicationContext.getSharedPreferences(HIVAwarenessActivity.PREFS, Context.MODE_PRIVATE);

        // Since visitor CV's should only be declared the first time an app runs, check if
        // it's run before. Add as necessary.
        final boolean firstRun = prefs.getBoolean(FIRST_RUN_KEY, true);
        if (firstRun) {
            Log.d(TAG, "Analytics firstRun");

    		//Get the app version
    		String version = "?";
    		int versionCode = 0;
    		try {
    			PackageManager pm = mApplicationContext.getPackageManager();
    		    PackageInfo pi = pm.getPackageInfo(mApplicationContext.getPackageName(), 0);
    		    version = pi.versionName;
    		    versionCode = pi.versionCode;
    		} catch (PackageManager.NameNotFoundException e) {
    			if (!HIVAwarenessActivity.DEBUG) FlurryAgent.onError("AnalyticsUtils:constructor", "(Package name not found for analytics)", e.getMessage());
    		    if (HIVAwarenessActivity.DEBUG) Log.e(TAG, "Package name not found", e);
    		};
    		final String versionString = version+" ("+versionCode+")";
    		
    		final String appFormat = "full";

            final String model = Build.MODEL;
    		
    		mTracker.setCustomVar(GA_INDEX_VISITOR_APP_VERSION, GA_KEY_APP_VERSION, versionString, GA_SCOPE_VISITOR);
    		mTracker.setCustomVar(GA_INDEX_VISITOR_APP_FORMAT, GA_KEY_APP_FORMAT, appFormat, GA_SCOPE_VISITOR);
    		mTracker.setCustomVar(GA_INDEX_VISITOR_PHONE, GA_KEY_PHONE, model, GA_SCOPE_VISITOR);

            // Close out so we never run this block again, unless app is removed & =
            // reinstalled.
			if (HIVAwarenessActivity.mSharedPreferences_Editor_apply_available){
				prefs.edit().putBoolean(FIRST_RUN_KEY, false).apply();
			}else{
				prefs.edit().putBoolean(FIRST_RUN_KEY, false).commit();
			}
        }
    }

    public void trackGAEvent(final String eventCategory, final String eventName, final String label,
            final int value) {
        try {
			// We wrap the call in an AsyncTask since the Google Analytics library writes to disk
			// on its calling thread.
			new AsyncTask<Void, Void, Void>() {
			    @Override
			    protected Void doInBackground(Void... voids) {
			        try {
			            mTracker.trackEvent(eventCategory, eventName, label, value);
			            Log.d(TAG, "Analytics trackEvent: "
			                    + eventCategory + " / " + eventName + " / " + label + " / " + value);//TODO: too much debugging logging
			        } catch (Exception e) {
			            // We don't want to crash if there's an Analytics library exception.
			            Log.w(TAG, "Analytics trackEvent error: "
			                    + eventCategory + " / " + eventName + " / " + label + " / " + value, e);
			        }
			        return null;
			    }
			}.execute();
		} catch (RejectedExecutionException e) {//BUGSOLVED: #796532
			//No room for another AsyncTask, will just drop it.
		}
    }

	public void trackGAEvent(String eventCategory, String eventName) {
		trackGAEvent(eventCategory, eventName, null, 0);
	}

    public void trackGAPageView(final String page, final String subpage) {
        try {
        	// We wrap the call in an AsyncTask since the Google Analytics library writes to disk
			// on its calling thread.
			new AsyncTask<Void, Void, Void>() {
			    @Override
			    protected Void doInBackground(Void... voids) {
			        try {
			    		mTracker.trackPageView(page);
			            Log.d(TAG, "Analytics trackPageView: " + page + "/ " + subpage);//TODO: too much debugging logging
			        } catch (Exception e) {
			            // We don't want to crash if there's an Analytics library exception.
			            Log.w(TAG, "Analytics trackPageView error: " + page + " / " + subpage, e);
			        }
			        return null;
			    }
			}.execute();
		} catch (RejectedExecutionException e) {//BUGSOLVED: #796532
			//No room for another AsyncTask, will just drop it.
		}
    }

    /**
     * Empty instance for use when Analytics is disabled or there was no Context available.
     */
    private static AnalyticsUtils sEmptyAnalyticsUtils = new AnalyticsUtils(null) {
        @Override
        public void trackGAEvent(String eventCategory, String eventName, String label, int value) {}

        @Override
        public void trackGAEvent(String eventCategory, String eventName) {}

        @Override
        public void trackGAPageView(String page, String product) {}

        @Override
    	public void stopTracker() {}
        
        @Override
    	public void dispatchTracker() {}
    };
    
	public void stopTracker() {
		if (mTracker!=null){
			mTracker.stop();
		}
	}
    
	public void dispatchTracker() {
		if (mTracker!=null){
			mTracker.dispatch();
		}
	}
}
