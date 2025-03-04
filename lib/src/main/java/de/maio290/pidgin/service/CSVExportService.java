package de.maio290.pidgin.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import de.maio290.pidgin.model.ChatMessage;

public class CSVExportService {

	public static void dumpMessages(Path output, List<ChatMessage> messages) throws IOException
	{
		List<String> lines = new ArrayList<>();
		lines.add(ChatMessage.getCSVHeader());
		
		for(var msg : messages)
		{
			lines.add(msg.toCSV());
		}
		
		Files.write(output.resolve("aggregated-messages.csv"), lines);	
	}
	
}
