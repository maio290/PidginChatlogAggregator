package de.maio290.pidgin.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.maio290.pidgin.enums.InstantMessagingService;
import de.maio290.pidgin.model.ChatMessage;

/**
 * Service class to parse the HTML chatlogs of Pidgin without using a DOM or HTML parser.
 * TODO: Currently, only ICQ is supported and the architecture would need an overhaul if more protocols were to be supported.
 */
public class HTMLParseService {

	
	public static final String OWN_COLOUR = "#16569E";
	public static final String PARTNER_COLOUR = "#A82F2F";
	
	public static final DateTimeFormatter FILE_NAME_DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd.HHmmssZz"); 
	public static final DateTimeFormatter CHAT_MESSAGE_TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss");
	public static final DateTimeFormatter CHAT_MESSAGE_DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss");
	
	static final Logger logger = LoggerFactory.getLogger(HTMLParseService.class);
	
	private HTMLParseService()
	{}
	
	
	//TODO: make the HTML fragments constants
	public static boolean validateLine(String line)
	{
		if(!line.contains("<font color=")) return false;
		if(!line.contains("<font size=\"2\">")) return false;
		if(!line.contains("<b>")) return false;
		if(!line.endsWith("<br/>")) return false;
		return true;
	}
	
	
	public static String extractContent(String target)
	{
		if(target == null || target.isBlank()) return null;
		final int start = target.lastIndexOf("</font>")+"</font>".length();
		String content = target.substring(start);
		content = content.replace("<br/>", " ").replaceAll("<[^>]*>", "");
		content = content.strip();
		return content;
	}
	
	public static String extractHTMLFragment(String target, String startFragment, String endFragment)
	{
		if(target == null || target.isBlank()) return null;
		logger.trace("Extracting: {} until {} from: {}", startFragment, endFragment, target);
		final int start = target.indexOf(startFragment)+startFragment.length();
		final int end = target.indexOf(endFragment, start);
		logger.trace("Resolved index - start: {}, end: {}", start, end);
		return target.substring(start, end).strip();
	}

	/**
	 * The chat files in HTML are structured as follows:
	 * <p>The file contains a header line (always the first) which can be discarded completely.</p>
	 * <p>Each chat message starts with a {@code <font>} tag and always ends with {@code <br/>} - 
	 * however, you cannot split it by {@code <br/>} because messages can contain paragraphs, 
	 * so {@code \n} is used, which is the default for {@code readAllLines}.</p>
	 * <p>The structure per line is:</p>
	 * <ul>
	 *     <li>{@code <font color="#COL">} - determines who wrote the message (OWN / PARTNER) 
	 *     [see {@link OWN_COLOUR} / {@link PARTNER_COLOUR}].</li>
	 *     <li>{@code <font size="2">(HH:mm:SS)</font>} - determines the sent date of the message.</li>
	 *     <li>{@code <b>Alias OR ICQ</b>} - determines the alias which was present when receiving the message.</li>
	 *     <li>{@code </font>} until the last {@code <br/>} - contains the actual message.</li>
	 * </ul>
	 */

	public static ChatMessage parseLine(String line, LocalDate fileDate, String ownICQ, String partnerICQ)
	{
		ChatMessage msg = new ChatMessage();
		msg.service = InstantMessagingService.ICQ;
		msg.isOwn = line.contains(OWN_COLOUR);
		
		String time = extractHTMLFragment(line, "<font size=\"2\">", "</font>");
		String alias = extractHTMLFragment(line, "<b>", "</b>");
		String content = extractContent(line);
		
		if(time == null || alias == null || content == null) return null;
		
		time = time.replace("(","").replace(")", "");
		
		LocalTime messageTime = null;
		LocalDateTime sentDate = null;
		
		if(time.matches("^\\d{2}:\\d{2}:\\d{2}$"))
		{
			messageTime = LocalTime.parse(time, CHAT_MESSAGE_TIME_FORMATTER);
			sentDate = LocalDateTime.of(fileDate, messageTime);
		}
		
		if(time.matches("^\\d{2}\\.\\d{2}\\.\\d{4} \\d{2}:\\d{2}:\\d{2}$"))
		{
			sentDate = LocalDateTime.parse(time, CHAT_MESSAGE_DATE_TIME_FORMATTER);
		}
		

		
		if(alias.endsWith(":")) alias = alias.substring(0, alias.length()-1);
		
		msg.author = alias;
		msg.authorPrimary = msg.isOwn ? ownICQ : partnerICQ;
		msg.partner = msg.isOwn ? partnerICQ : ownICQ;
		msg.content = content;
		msg.date = sentDate;
				
		return msg;
	}
	
	public static List<ChatMessage> parseFile(Path path)
	{
		List<ChatMessage> messages = new ArrayList<ChatMessage>();
		
		List<String> lines = null;
		try 
		{
			lines = Files.readAllLines(path);
		} 
		catch (IOException e)
		{
			logger.error("Failed to read lines from: {} - exception:", path, e);
			return messages;
		}
		
		logger.debug("Read {} lines from {}", lines.size(), path.toAbsolutePath());
		
		
		String partnerICQ = path.getParent().toString();
		partnerICQ = partnerICQ.substring(partnerICQ.lastIndexOf("\\")).replace("\\", "");
		
		String ownICQ = path.getParent().getParent().toString();
		ownICQ = ownICQ.substring(ownICQ.lastIndexOf("\\")).replace("\\", "");
		
		
		String filenameExtensionless = path.toFile().getName().replace(".html", "");
		LocalDate chatDate = LocalDateTime.parse(filenameExtensionless, FILE_NAME_DATE_FORMATTER).toLocalDate();
		
		
		// Closing- and Endtags are skipped
		for(int i = 1; i<lines.size()-1; i++)
		{
			final String line = lines.get(i);
			
			if(!validateLine(line))
			{
				logger.error("The file {} contained an invalid line at index {}: {}", path, i, line);
				continue;
			}
			
			messages.add(parseLine(line, chatDate, ownICQ, partnerICQ));
		}

		return messages;
	}
}
