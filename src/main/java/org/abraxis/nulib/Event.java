package org.abraxis.nulib;


import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;

import java.io.IOException;

public class Event
{
	private transient Logger logger = Log.getLogger(this.getClass());
	private EventType eventType;
	private String device;
	private String usbDevice;
	private String message;

	public Event()
	{
	}

	public Event(EventType eventType)
	{
		this();
		this.eventType = eventType;
		this.message = "Event type: " + eventType.toString();
	}

	public String toJSON()
	{
		String json = "";
		try {
			ObjectMapper mapper = new ObjectMapper();
			json = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(this);
			logger.debug("Serialized to JSON as: {}", json);
		} catch (Exception ex) {
			logger.error("Error while trying to serialize Event to JSON", ex);
		}
		return json;
	}

	public static Event fromJSON(String json)
	{
		Event event = null;
		try {
			ObjectMapper mapper = new ObjectMapper();
			event = mapper.readValue(json, Event.class);
			Log.getLogger(Event.class).debug("Serialized from JSON: {}", json);
		} catch (Exception ex) {
			Log.getLogger(Event.class).error("Error while trying to deserialize Event from JSON", ex);
		}
		return event;
	}

	public void emit() throws IOException
	{
		Bus bus = new Bus();
		Config c = Config.getInstance();

		String msg = this.toJSON();
		String routingKey = eventType.toString();

		bus.emitEvent(msg, routingKey);

		Log.getLogger(this.getClass()).info("Sent '" + routingKey + "':'" + message + "'");
	}

	public EventType getEventType()
	{
		return eventType;
	}

	public void setEventType(EventType eventType)
	{
		this.eventType = eventType;
	}

	public String getDevice()
	{
		return device;
	}

	public void setDevice(String device)
	{
		this.device = device;
	}

	public String getUsbDevice()
	{
		return usbDevice;
	}

	public void setUsbDevice(String usbDevice)
	{
		this.usbDevice = usbDevice;
	}

	public String getMessage()
	{
		return message;
	}

	public void setMessage(String message)
	{
		this.message = message;
	}
}
