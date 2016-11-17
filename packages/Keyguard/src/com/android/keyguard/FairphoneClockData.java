package com.android.keyguard;

import com.android.internal.app.IBatteryStats;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.BatteryStats;
import android.os.ServiceManager;
import android.os.RemoteException;
import android.util.Log;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

/**
 * Created by rrocha on 8/25/15.
 */
public class FairphoneClockData
{
	private static final String TAG = FairphoneClockData.class.getSimpleName();
	private static final String BOARD_DATE_FILE = "/persist/board_date.bin";
	private static final String FAIRPHONE_CLOCK_PREFERENCES = "com.fairphone.clock.FAIRPHONE_CLOCK_PREFERENCES";
	private static final String PREFERENCE_BATTERY_LEVEL = "com.fairphone.clock.PREFERENCE_BATTERY_LEVEL";
	private static final String PREFERENCE_ACTIVE_LAYOUT = "com.fairphone.clock.PREFERENCE_ACTIVE_LAYOUT";
	private static final String PREFERENCE_BATTERY_STATUS = "com.fairphone.clock.PREFERENCE_BATTERY_STATUS";
	private static final String PREFERENCE_POM_CURRENT = "com.fairphone.clock.PREFERENCE_POM_CURRENT";
	private static final String PREFERENCE_POM_RECORD = "com.fairphone.clock.PREFERENCE_POM_RECORD";
	private static final String PREFERENCE_DEVICE_BIRTHDATE = "com.fairphone.clock.PREFERENCE_DEVICE_BIRTHDATE";
	private static final String PREFERENCE_BATTERY_CHANGED_TIMESTAMP = "com.fairphone.clock.PREFERENCE_BATTERY_CHANGED_TIMESTAMP";
	private static final String PREFERENCE_BATTERY_TIME_UNTIL_DISCHARGED = "com.fairphone.clock.PREFERENCE_BATTERY_TIME_UNTIL_DISCHARGED";
	private static final String PREFERENCE_BATTERY_TIME_UNTIL_CHARGED = "com.fairphone.clock.PREFERENCE_BATTERY_TIME_UNTIL_CHARGED";

	/*
	 * Legacy preference that stored the board date and fell back to 2015-10-26 if no board date (prototype devices).
	 *
	 * private static final String PREFERENCE_YOUR_FAIRPHONE_SINCE = "com.fairphone.clock.PREFERENCE_YOUR_FAIRPHONE_SINCE";
	 */

	public static final String VIEW_UPDATE = "com.fairphone.fairphoneclock.UPDATE";

	private static final String BOARD_DATE_FORMAT = "yyyyMMddhhmmss";
	private static final String BOARD_DATE_TIMEZONE = "Asia/Shanghai";
	/* Keep consistency and default to 2015-10-26. */
	private static final long DEFAULT_BOARD_DATE_TIMESTAMP = 1445817600L;

	private static SharedPreferences getSharedPrefs(Context context)
	{
		return context.getSharedPreferences(FAIRPHONE_CLOCK_PREFERENCES, Context.MODE_PRIVATE);
	}

	public static long getPeaceOfMindCurrent(Context context)
	{
		return getSharedPrefs(context).getLong(PREFERENCE_POM_CURRENT, 0);
	}

	public static void setPeaceOfMindCurrent(Context context, long value)
	{
		getSharedPrefs(context).edit().putLong(PREFERENCE_POM_CURRENT, value).commit();
	}

	public static long getPeaceOfMindRecord(Context context)
	{
		return getSharedPrefs(context).getLong(PREFERENCE_POM_RECORD, 0);
	}

	public static void setPeaceOfMindRecord(Context context, long value)
	{
		getSharedPrefs(context).edit().putLong(PREFERENCE_POM_RECORD, value).commit();
	}

	public static int getBatteryLevel(Context context)
	{
		return getSharedPrefs(context).getInt(PREFERENCE_BATTERY_LEVEL, 0);
	}

	public static void setBatteryLevel(Context context, int value)
	{
		getSharedPrefs(context).edit().putInt(PREFERENCE_BATTERY_LEVEL, value).commit();
	}

	public static int getBatteryStatus(Context context)
	{
		return getSharedPrefs(context).getInt(PREFERENCE_BATTERY_STATUS, 0);
	}

	public static void setBatteryStatus(Context context, int value)
	{
		getSharedPrefs(context).edit().putInt(PREFERENCE_BATTERY_STATUS, value).commit();
	}

	public static long getBatteryTimeUntilCharged(Context context)
	{
		return getSharedPrefs(context).getLong(PREFERENCE_BATTERY_TIME_UNTIL_CHARGED, 0);
	}

	public static void setBatteryTimeUntilCharged(Context context, long value)
	{
		getSharedPrefs(context).edit().putLong(PREFERENCE_BATTERY_TIME_UNTIL_CHARGED, value).commit();
	}

	public static long getBatteryTimeUntilDischarged(Context context)
	{
		return getSharedPrefs(context).getLong(PREFERENCE_BATTERY_TIME_UNTIL_DISCHARGED, 0);
	}

	public static void setBatteryTimeUntilDischarged(Context context, long value)
	{
		getSharedPrefs(context).edit().putLong(PREFERENCE_BATTERY_TIME_UNTIL_DISCHARGED, value).commit();
	}

	public static int getCurrentLayoutIdx(Context context)
	{
		return getSharedPrefs(context).getInt(PREFERENCE_ACTIVE_LAYOUT, 0);
	}

	public static void setCurrentLayoutIdx(Context context, int value)
	{
		getSharedPrefs(context).edit().putInt(PREFERENCE_ACTIVE_LAYOUT, value).commit();
	}

	public static long getDeviceBirthdate(Context context)
	{
		long birthdate = getSharedPrefs(context).getLong(PREFERENCE_DEVICE_BIRTHDATE, 0L);

		if (birthdate == 0L) {
			birthdate = readBoardDate();
			setDeviceBirthdate(context, birthdate);
		}

		return birthdate;
	}

	public static void setDeviceBirthdate(Context context, long value)
	{
		getSharedPrefs(context).edit().putLong(PREFERENCE_DEVICE_BIRTHDATE, value).commit();
	}

    private static IBatteryStats sBatteryInfo = IBatteryStats.Stub.asInterface(ServiceManager.getService(BatteryStats.SERVICE_NAME));
    public static void updateBatteryPreferences(Context context, int level, int status, int scale)
    {
		setBatteryStatus(context.getApplicationContext(), status);
		setBatteryLevel(context.getApplicationContext(), level);
		try {
			long chargeTimeRemaining = sBatteryInfo.computeChargeTimeRemaining();
			if (chargeTimeRemaining >= 0) {
			setBatteryTimeUntilCharged(context.getApplicationContext(), chargeTimeRemaining);
			}
			long batteryTimeRemaining = sBatteryInfo.computeBatteryTimeRemaining();
			if (batteryTimeRemaining >= 0) {
				setBatteryTimeUntilDischarged(context.getApplicationContext(), batteryTimeRemaining);
			}
	        Log.d(TAG, "updateBatteryPreferences setBatteryTimeUntilCharged "+chargeTimeRemaining);
	        Log.d(TAG, "updateBatteryPreferences setBatteryTimeUntilDischarged "+batteryTimeRemaining);
		} catch (RemoteException e) {
			Log.e(TAG, "failed to updateBatteryPreferences", e);
		}
//	    int currentLevel = getBatteryLevel(context);
//	    int currentStatus = getBatteryStatus(context);
//
//	    if (currentLevel != level || currentStatus != status)
//	    {
//		    setBatteryLevel(context, level);
//		    setBatteryStatus(context, status);
//		    updateBatteryDurationTimes(context, level, scale);
//	    }
    }

	public static void sendLastLongerBroadcast(Context context)
	{
		FairphoneClockView.dismissKeyguardOnNextActivity();
		context.startActivity(new Intent(Intent.ACTION_POWER_USAGE_SUMMARY).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
	}

	public static void sendUpdateBroadcast(Context context)
	{
		context.sendBroadcast(new Intent(VIEW_UPDATE));
	}

    private static long readBoardDate() {
		long boardDate = 0;

		try {
			final byte[] boardFile = getBoardDateFileInByteArray();
			final String strDate = bytesToHex(boardFile);
			final SimpleDateFormat format = new SimpleDateFormat(BOARD_DATE_FORMAT, Locale.US);

			format.setTimeZone(TimeZone.getTimeZone(BOARD_DATE_TIMEZONE));
			boardDate = format.parse(strDate.substring(0,14)).getTime();
		} catch (Exception e) {
			Log.e(TAG, "Unknown error while reading board date; using default", e);
			boardDate = DEFAULT_BOARD_DATE_TIMESTAMP;
		}

		return boardDate;
	}

	private static byte[] getBoardDateFileInByteArray() {
		final File file = new File(BOARD_DATE_FILE);
		final byte[] fileData = new byte[(int) file.length()];
		DataInputStream dis = null;

		try {
			dis = new DataInputStream(new FileInputStream(file));
			dis.read(fileData);
			dis.close();
		} catch (FileNotFoundException e) {
			Log.e(TAG,"BoardDateFile not found", e);
		} catch (IOException e) {
			Log.e(TAG, "BoardDateFile IOException", e);
		}
		finally {
			return fileData;
		}
	}

	private static final char[] hexArray = "0123456789ABCDEF".toCharArray();
	private static String bytesToHex(byte[] bytes) {
		final char[] hexChars = new char[bytes.length * 2];

		for (int j = 0; j < bytes.length; j++) {
			int v = bytes[j] & 0xFF;
			hexChars[j * 2] = hexArray[v >>> 4];
			hexChars[j * 2 + 1] = hexArray[v & 0x0F];
		}

		return new String(hexChars);
	}
}
