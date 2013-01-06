package org.abraxis.nulib;

import java.io.IOException;

public class Event
{
	public static final String MQ_TOPIC_EVENTS_KEY = "mq.topic_events";
	private EventType eventType;
	private String device;
	private String message;

	public Event()
	{
	}

	public Event(EventType eventType)
	{
		this.eventType = eventType;
		this.message = "Event type: " + eventType.toString();
	}

	public String toJSON()
	{
		String json = "";
//        try {
//            json = JsonWriter.objectToJson(this);
//        } catch (IOException ex) {
//            Log.getLogger(Event.class).error("Error while trying to serialize Event to JSON", ex);
//        }
		return json;
	}

	public static Event fromJSON(String json)
	{
		Event event = null;
//        try {
//            event = (Event) JsonReader.jsonToJava(json);
//        } catch (IOException ex) {
//            Log.getLogger(Event.class).error("Error while trying to deserialize Event from JSON", ex);
//        }
		return event;
	}

	public void emitEvent() throws IOException
	{
		Bus bus = new Bus();
		Config c = Config.getInstance();

		String msg = this.toJSON();
		String topic = c.getProperty(MQ_TOPIC_EVENTS_KEY);
		String routingKey = eventType.toString();

		bus.emitMessage(topic, msg, routingKey);

		Log.getLogger(this.getClass()).info("Sent '" + routingKey + "':'" + message + "'");
	}

	public EventType getEventType()
	{
		return eventType;
	}

	public String getDevice()
	{
		return device;
	}

	public String getMessage()
	{
		return message;
	}
}
