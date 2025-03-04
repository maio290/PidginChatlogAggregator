package de.maio290.pidgin.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import de.maio290.pidgin.enums.InstantMessagingService;

public class ChatMessage {
	
	public ChatMessage()
	{}
	
	public InstantMessagingService service;
	public LocalDateTime date;
	public String author;
	public String authorPrimary;
	public String partner;
	public String content;
	public boolean isOwn;
	
	public static String getCSVHeader()
	{
		return "\ufeffservice;date;author;authorPrimary;partner;content;isOwn";
	}
	
	public String toCSV()
	{
		StringBuilder csv = new StringBuilder();
		csv.append(service.name()).append(";");
		csv.append(date.format(DateTimeFormatter.ISO_DATE_TIME)).append(";");
		csv.append("\"").append(author).append("\"").append(";");
		csv.append(authorPrimary).append(";");
		csv.append(partner).append(";");
		csv.append("\"").append(content.replace('"', '\'')).append("\"").append(";");
		csv.append(isOwn).append(";");
		return csv.toString();
	}
	
}
