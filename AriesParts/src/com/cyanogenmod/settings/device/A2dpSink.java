/*
 * Copyright (C) 2014 The CyanogenMod Project
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

package com.cyanogenmod.settings.device;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.SystemProperties;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;
import android.bluetooth.BluetoothAdapter;

import com.cyanogenmod.settings.device.R;

public class A2dpSink implements OnPreferenceChangeListener {

	// Logging tag
	private static final String TAG = "DeviceSettings_A2dpSink";

	// Keys of used system properties
	private static final String SYSTEM_VERSION_PROP_KEY = "ro.build.version.release";
	private static final String A2DPSINK_PROP_KEY = "persist.service.bt.a2dp.sink";

	// A2dpSink property is supported starting from Android 4.4 (KitKat)
	private static final float MIN_VERSION_SUPPORTED = 4.4f;

	/**
	 * Checks if the feature is supported
	 * @return True if the feature is supported, otherwise false
	 */
	public static boolean isSupported() {
		// Get the system version
		String versionString = BuildProp.get(SYSTEM_VERSION_PROP_KEY);

		// Return true if the system is Android 4.4 (KitKat) or above
		if (versionString != null && !versionString.isEmpty()) {
			int index = versionString.indexOf(".");
			float version = Float.parseFloat((index > 0 && versionString.length() >= index + 2) ?
					versionString.substring(0, index + 2) : versionString);
			return version >= MIN_VERSION_SUPPORTED;
		} else {
			return false;
		}
	}

	/**
	 * Gets the current value of A2dpSink property inside system properties
	 * @return The A2dpSink value currently set
	 */
	public static boolean getPropertyValue() {
		return SystemProperties.getBoolean(A2DPSINK_PROP_KEY, false);
	}

	/**
	 * Stores the value of A2dpSink property inside system properties
	 * @param value The value to store
	 * @return True if the store succeeded, otherwise false
	 */
	private static boolean storePropertyValue(Boolean value) {
		SystemProperties.set(A2DPSINK_PROP_KEY, value.toString());
		return true;
	}

	/**
	 * Shows the error dialog for an update failure
	 * @param context The context of this Preference
	 */
	private void showErrorDialog(final Context context, int msgId) {
		AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(context);
		dialogBuilder.setTitle(R.string.a2dp_sink_title)
			.setMessage(msgId)
			.create()
			.show();
	}

	@Override
	public boolean onPreferenceChange(Preference preference, Object value) {
		// Get the preference context
		final Context context = preference.getContext();

		BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
		if (mBluetoothAdapter == null) {
			// device does not support Bluetooth (should never happen)
			return false;
		}
		if (mBluetoothAdapter.isEnabled()) {
			// although the a2dp.sink property can be changed anytime, the change will
			// only be effective when the bluetooth service is (re-)started.
			// As the user has to restart bluetooth in any case, allow changes to the settings only if
			// bluetooth is off. This way we also prevent side effects which might occur if the property
			// is changed while bluetooth is active.
			// Note that for some reason Android does not allow sink and source at the same time.
			showErrorDialog(context, R.string.a2dp_sink_error_bt_active);
			return false;
		}

		// Store the new value
		return storePropertyValue((Boolean)value);
	}

}
