package com.zdonnell.eve;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

public class Tools {
	
	private static long D_MILLIS = 24 * 60 * 60 * 1000;
	private static long H_MILLIS = 60 * 60 * 1000;
	private static long M_MILLIS = 60 * 1000;
	
	/**
	 * Check and see how much time until the UTC Time string provided
	 * 
	 * @param UTCString the time to check against in the format yyyy-MM-dd HH:mm:ss
	 * @return the time until the UTC Time specified in milliseconds
	 * @throws ParseException
	 */
	public static long timeUntilUTCTime(String UTCString) throws ParseException
	{
		Calendar now = Calendar.getInstance();
		
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		formatter.setTimeZone(TimeZone.getTimeZone("UTC"));
		Date thenDate = new Date();
		
		thenDate = formatter.parse(UTCString);
	
		Calendar then =  Calendar.getInstance();
		then.setTime(thenDate);
		
		return (then.getTimeInMillis() - now.getTimeInMillis());
	}
	
	/**
	 * Converts a tiem value into the specified String representation
	 * 
	 * @param millis the time in milliseconds to format
	 * @return a string representation of the time, fomatted xxd xxh xxm xxs
	 */
	public static String millisToEveFormatString(long millis)
	{		
		long daysRounded = (int) Math.floor(millis / (24 * 60 * 60 * 1000));
		long hoursRounded = (int) Math.floor((millis - (daysRounded * D_MILLIS)) / (60 * 60 * 1000));
		long minutesRounded = (int) Math.floor((millis - (daysRounded * D_MILLIS) - (hoursRounded * H_MILLIS)) / (60 * 1000));
		long secondsRounded = (int) Math.floor((millis - (daysRounded * D_MILLIS) - (hoursRounded * H_MILLIS) - (minutesRounded * M_MILLIS)) / (1000));

		String formattedString = "";
		
		if (daysRounded != 0) formattedString += daysRounded + "d ";
		if (hoursRounded != 0) formattedString += hoursRounded + "h ";
		if (minutesRounded != 0) formattedString += minutesRounded + "m ";
		if (secondsRounded != 0) formattedString += secondsRounded + "s ";
		
		return formattedString;
	}
}