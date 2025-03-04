package de.maio290.pidgin.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.maio290.pidgin.model.ChatMessage;

/**
 * Service class to parse Pidgin's chat log folder (.purple\logs\*)
 * TODO: Only supports ICQ currently.
 */
public class MessageFolderParseService {
	
	public static final boolean IGNORE_GROUPS = true;
	
	static final Logger logger = LoggerFactory.getLogger(MessageFolderParseService.class);
	
	public static List<ChatMessage> parseICQSubfolder(Path path) throws IOException
	{
		List<Path> chatlogs = Files.walk(path, 1).filter(p -> !Files.isDirectory(p)).collect(Collectors.toList());
		logger.debug("Found {} converaations in {}", chatlogs.size(), path);
		
		List<ChatMessage> messages = new ArrayList<>();
		
		for(var chatlog : chatlogs)
		{
			logger.debug("Parsing file: {}",  chatlog);
			messages.addAll(HTMLParseService.parseFile(chatlog));
		}
		
		
		return messages;
	}
	
	public static List<ChatMessage> parseICQFolder(Path folder) throws IOException
	{
		
		List<Path> chatFolders = null;
		List<ChatMessage> messages = new ArrayList<>();
		
		if(IGNORE_GROUPS)
		{
			chatFolders = Files.walk(folder, 1).filter(p -> Files.isDirectory(p) && !p.endsWith(".system") && !p.getFileName().toString().endsWith(".chat")).collect(Collectors.toList());
		}
		else
		{
			chatFolders = Files.walk(folder, 1).filter(p -> Files.isDirectory(p) && !p.endsWith(".system")).collect(Collectors.toList());
		}

		logger.info("Found {} different chat partners in {}", chatFolders.size(), folder);
		
		
		for(var chatFolder : chatFolders)
		{
			List<ChatMessage> parsedMessages = parseICQSubfolder(chatFolder);
			messages.addAll(parsedMessages);
			logger.debug("Parsed {} messages from {}", parsedMessages.size(), chatFolder);
		}
		
		
		logger.info("Parsed {} messages", messages.size());
		
		return messages;
	}
}
