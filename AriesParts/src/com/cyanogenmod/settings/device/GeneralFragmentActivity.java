/*
 * Copyright (C) 2013 The CyanogenMod Project
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

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemProperties;
import android.os.FileObserver;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.util.Log;
import android.view.Gravity;
import android.widget.Toast;
import android.widget.CheckBox;
import android.util.Log;

import com.cyanogenmod.settings.device.R;

public class GeneralFragmentActivity extends PreferenceFragment {

    private static final String TAG = "DeviceSettings_General";

    private Preference mGSensor;
    private ListPreference mOTGCharge;
    private CheckBoxPreference mFastCharge;
    private CheckBoxPreference mLowRamStatus;
    private CheckBoxPreference mA2dpSinkStatus;
    private FileObserver mObserver;

    private Handler mHandler = new Handler();

    private final Runnable mFileChangedRunnable = new Runnable() {
        @Override
        public void run() {
            boolean mNewFeatureValue = FastCharge.isActive();
            onFileChanged(mNewFeatureValue);
            mFastCharge.setChecked(mNewFeatureValue);
        }
    };

    /**
     * subclasses can override onFileChanged() to hook
     * into the FileObserver onEvent() callback
     */

    protected void onFileChanged(boolean featureState){}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.general_preferences);

        PreferenceScreen prefSet = getPreferenceScreen();
        Context mContext = getActivity();

        mGSensor = (Preference) findPreference(DeviceSettings.KEY_GSENSOR);
        mGSensor.setEnabled(GSensor.isSupported());

        mFastCharge = (CheckBoxPreference) findPreference(DeviceSettings.KEY_FAST_CHARGE);
        mFastCharge.setEnabled(FastCharge.isSupported());
        mFastCharge.setOnPreferenceChangeListener(new FastCharge(mContext));

        mOTGCharge = (ListPreference) findPreference(DeviceSettings.KEY_OTG_CHARGE);
        mOTGCharge.setEnabled(OTGCharge.isSupported());
        mOTGCharge.setOnPreferenceChangeListener(new OTGCharge());
        
        mObserver = new FileObserver(FastCharge.getFilePath(), FileObserver.MODIFY) {
            @Override
            public void onEvent(int event, String file) {
                mHandler.postDelayed(mFileChangedRunnable, 1);
            }
        };
        mObserver.startWatching();

        mLowRamStatus = (CheckBoxPreference) findPreference(DeviceSettings.KEY_LOW_RAM);
        mLowRamStatus.setEnabled(LowRam.isSupported());
        mLowRamStatus.setOnPreferenceChangeListener(new LowRam());

        mA2dpSinkStatus = (CheckBoxPreference) findPreference(DeviceSettings.KEY_A2DP_SINK);
        mA2dpSinkStatus.setEnabled(A2dpSink.isSupported());
        mA2dpSinkStatus.setOnPreferenceChangeListener(new A2dpSink());
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mLowRamStatus.setChecked(LowRam.getPropertyValue());
        mA2dpSinkStatus.setChecked(A2dpSink.getPropertyValue());
        mFastCharge.setChecked(FastCharge.isActive());
    }

    @Override
    public void onDestroy() {
        mObserver.stopWatching();
        super.onDestroy();
    }

    public static void restore(Context context) {
        //FastCharge.restore(context);
        OTGCharge.restore(context);
    }
}
