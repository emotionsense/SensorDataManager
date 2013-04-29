package com.ubhave.datahandler.alarm;

import android.content.Intent;

public interface AlarmListener
{
	public void alarmTriggered();
	public boolean intentMatches(final Intent intent);
}
