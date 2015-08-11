package com.ubhave.datahandler.transfer.alarm;

import android.content.Intent;

public interface AlarmListener
{
	/*
	 * alarmTriggered() will be called when either:
	 * (a) Any connection is available and the waitForWifiInterval has been exceeded
	 * (b) A wifi connection is available and the alarmInterval has been exceeded
	 */
	public void alarmTriggered();
	
	/*
	 * intentMatches() is used to check whether the alarm is relevant to the given listener
	 * e.g., if you have multiple files to sync.
	 */
	public boolean intentMatches(final Intent intent);
}
