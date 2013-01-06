package org.abraxis.nulib;

import com.rabbitmq.client.AMQP.BasicProperties;
import com.rabbitmq.client.AMQP.BasicProperties.Builder;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.QueueingConsumer;
import com.rabbitmq.client.QueueingConsumer.Delivery;
import org.slf4j.Logger;

import java.io.IOException;

public class Bus
{
	public static final String MQ_HOST_CONFIG_KEY = "mq.host";
	public static final String MQ_LOGIN_KEY = "mq.login";
	public static final String MQ_PASSWORD_KEY = "mq.password";
	public static final String MQ_EXCHANGE_EVENTS_KEY = "mq.exchange_events";
	public static final String MQ_EXCHANGE_RPC_KEY = "mq.exchange_rpc";
	public static final String DEFAULT_CONTENT_TYPE = "application/json";
	public static final String DEFAULT_ENCODING = "UTF-8";

	private Logger logger = Log.getLogger(this.getClass());
	private Channel channel;

	public Bus()
	{
		Config c = Config.getInstance();
		String host = c.getProperty(MQ_HOST_CONFIG_KEY);
		ConnectionFactory factory = new ConnectionFactory();
		factory.setHost(host);
		factory.setUsername(c.getProperty(MQ_LOGIN_KEY));
		factory.setPassword(c.getProperty(MQ_PASSWORD_KEY));
		try {
			logger.debug("Trying to connect");
			Connection connection = factory.newConnection();
			channel = connection.createChannel();
			logger.info("Bus connected to host {}", host);
		} catch (IOException ex) {
			logger.error("Error while trying to initialize message Bus", ex);
		}
	}

	public void closeConnection()
	{
		try {
			channel.getConnection().close();
		} catch (IOException ex) {
			logger.error("Error while trying to close connection", ex);
		}
	}

	public void emitEvent(String body, String routingKey) throws IOException
	{
		String exchange = Config.getInstance().getProperty(MQ_EXCHANGE_EVENTS_KEY);
		logger.debug("Trying to send event to exchange: {}", exchange);
		channel.exchangeDeclare(exchange, "topic", true);
		BasicProperties props = new Builder()
				.contentType(DEFAULT_CONTENT_TYPE)
				.contentEncoding(DEFAULT_ENCODING)
				.build();
		channel.basicPublish(exchange, routingKey, props, body.getBytes());
		logger.info("Send event with routingKey: {}, body: {}", routingKey, body);
	}

	public QueueingConsumer subscribeToEvents() throws IOException
	{
		String[] bindingKeys = {"#"};
		return subscribeToEvents(bindingKeys);
	}

	public String simpleGetEvent() throws IOException, InterruptedException
	{
		String[] bindingKeys = {"#"};
		return simpleGetEvent(bindingKeys);
	}

	private String simpleGetEvent(String[] bindingKeys) throws InterruptedException, IOException
	{
		QueueingConsumer consumer = subscribeToEvents(bindingKeys);
		return getEvent(consumer);
	}

	public QueueingConsumer subscribeToEvents(String[] bindingKeys) throws IOException
	{
		String exchange = Config.getInstance().getProperty(MQ_EXCHANGE_EVENTS_KEY);
		logger.info("Subscribing to events, exchange: {}", exchange);
		String queueName = channel.queueDeclare().getQueue();
		for (String bindingKey : bindingKeys) {
			channel.queueBind(queueName, exchange, bindingKey);
			logger.debug("Binding to exchange: {}, key: {}, queue: {}", exchange, bindingKey, queueName);
		}
		QueueingConsumer consumer = new QueueingConsumer(channel);
		channel.basicConsume(queueName, true, consumer);
		logger.debug("Returning consumer");
		return consumer;
	}

	public String getEvent(QueueingConsumer consumer) throws InterruptedException
	{
		logger.info("Trying to get an event");
		QueueingConsumer.Delivery delivery = consumer.nextDelivery();
		logger.trace("OK, got event");
		if (delivery == null) {
			logger.error("Got null delivery! Should never occur!");
			return "";  // Safest option...
		}
		String body = new String(delivery.getBody());
		String routingKey = delivery.getEnvelope().getRoutingKey();
		logger.info("Received event with routing key: {}, body: {}", routingKey, body);
		return body;
	}

	public String callRPC(String service, String body) throws IOException, InterruptedException
	{
		QueueingConsumer consumer;
		String exchange = Config.getInstance().getProperty(MQ_EXCHANGE_RPC_KEY);
		String response = null;
		String replyQueueName = channel.queueDeclare().getQueue();
		String corrId = java.util.UUID.randomUUID().toString();
		logger.debug("Calling RPC service: {}, exchange: {}, correlation ID: {}, reply queue: {}, body: {}", service, exchange, corrId, replyQueueName, body);

		// Init exchange
		channel.exchangeDeclare(exchange, "topic", true);
		// Prepare consumer for reply
		consumer = new QueueingConsumer(channel);
		channel.basicConsume(replyQueueName, true, consumer);

		BasicProperties props = new Builder()
				.correlationId(corrId)
				.replyTo(replyQueueName)
				.build();
		// Send message
		channel.basicPublish(exchange, service, props, body.getBytes());
		logger.trace("Message sent");
		while (true) {
			QueueingConsumer.Delivery delivery = consumer.nextDelivery();
			if (delivery.getProperties().getCorrelationId().equals(corrId)) {
				response = new String(delivery.getBody(), "UTF-8");
				break;
			} else {
				logger.warn("Received message but with different correlation ID! Correlation ID: {}, body: {}", delivery.getProperties().getCorrelationId(), delivery.getBody());
			}
		}
		logger.debug("Received reply: {}", response);
		return response;
	}

	public QueueingConsumer subscribeToRPC(String[] services) throws IOException
	{
		String exchange = Config.getInstance().getProperty(MQ_EXCHANGE_RPC_KEY);
		logger.info("Subscribing to RPC, exchange: {}", exchange);
		// Init exchange
		channel.exchangeDeclare(exchange, "topic", true);
		String queueName = channel.queueDeclare().getQueue();
		for (String bindingKey : services) {
			channel.queueBind(queueName, exchange, bindingKey);
			logger.debug("Binding to exchange: {}, key: {}, queue: {}", exchange, bindingKey, queueName);
		}
		QueueingConsumer consumer = new QueueingConsumer(channel);
		channel.basicConsume(queueName, true, consumer);
		logger.debug("Returning consumer");
		return consumer;
	}

	public Delivery getRPCRequest(QueueingConsumer consumer) throws InterruptedException
	{
		logger.info("Trying to get an RPC event");
		QueueingConsumer.Delivery delivery = consumer.nextDelivery();
		logger.trace("OK, got event");
		if (delivery == null) {
			logger.error("Got null delivery! Should never occur!");
			return null;
		}
		String routingKey = delivery.getEnvelope().getRoutingKey();
		logger.info("Received request for service key: {}, body: {}", routingKey, new String(delivery.getBody()));
		return delivery;
	}

	public void sendRPCReply(String body, String correlationID, String replyQueue) throws IOException
	{
		logger.debug("Sending RPC reply, correlation ID: {}, reply queue: {}, body: {}", correlationID, replyQueue, body);
		BasicProperties replyProps = new BasicProperties
				.Builder()
				.correlationId(correlationID)
				.build();

		channel.basicPublish("", replyQueue, replyProps, body.getBytes());
		logger.debug("Reply sent");
	}
}
