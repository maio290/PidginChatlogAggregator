package de.maio290.pidgin;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Scanner;

import de.maio290.pidgin.service.CSVExportService;
import de.maio290.pidgin.service.MessageFolderParseService;

public class PidginChatlogAggregator {
	
	public static void main(String[] args) throws IOException
	{
		System.out.print("Please enter your input folder (e.g. .purple\\logs\\icq\\34364143: ");
		
		try(Scanner scanner = new Scanner(System.in))
		{
			String input = scanner.next();
			System.out.print("Please enter your output file folder (e.g. C:\\Chatlogs\\: ");
			String output = scanner.next();
			
			Path inputFolder = Paths.get(input);
			Path outputFile = Paths.get(output);
			
			var messages = MessageFolderParseService.parseICQFolder(inputFolder);	
			CSVExportService.dumpMessages(outputFile, messages);
		}		
	}
}
