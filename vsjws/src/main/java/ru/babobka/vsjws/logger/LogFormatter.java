package ru.babobka.vsjws.logger;

/**
 * Created by dolgopolov.a on 12.01.16.
 */

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Date;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;

final class LogFormatter extends Formatter {

	private static final String LINE_SEPARATOR = System.getProperty("line.separator");

	@Override
	public String format(LogRecord record) {
		StringBuilder sb = new StringBuilder();

		sb.append(new Date(record.getMillis())).append(" ").append(record.getLevel().getLocalizedName()).append(": ")
				.append(formatMessage(record)).append(LINE_SEPARATOR);

		if (record.getThrown() != null) {
			try { 
				StringWriter sw = new StringWriter();
				PrintWriter pw = new PrintWriter(sw);
				record.getThrown().printStackTrace(pw);
				pw.close();
				sb.append(sw.toString());
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}

		return sb.toString();
	}
}