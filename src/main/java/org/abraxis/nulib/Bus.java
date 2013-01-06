package org.abraxis.nulib;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.QueueingConsumer;

import java.io.IOException;

import org.slf4j.Logger;

public class Bus
{
	public static final String MQ_HOST_CONFIG_KEY = "mq.host";

	private Logger logger = Log.getLogger(this.getClass());
	private Channel channel;

	public Bus()
	{
		Config c = Config.getInstance();
		ConnectionFactory factory = new ConnectionFactory();
		factory.setHost(c.getProperty(MQ_HOST_CONFIG_KEY));

		try {
			Connection connection = factory.newConnection();
			channel = connection.createChannel();
		} catch (IOException ex) {
			logger.error("Error while trying to initialize message Bus", ex);
		}
	}

	public void emitMessage(String topic, String message, String routingKey) throws IOException
	{
		channel.exchangeDeclare(topic, "topic");
		channel.basicPublish(topic, routingKey, null, message.getBytes());
		logger.info("Sent '" + routingKey + "':'" + message + "'");
	}

	public void closeConnection()
	{
		try {
			channel.getConnection().close();
		} catch (IOException ex) {
			logger.error("Error while trying to close connection", ex);
		}
	}

	public String getMessage(String topic) throws IOException, InterruptedException
	{
		String[] bindingKeys = {"#"};
		return getMessage(topic, bindingKeys);
	}

	public String getMessage(String topic, String[] bindingKeys) throws IOException, InterruptedException
	{
		channel.exchangeDeclare(topic, "topic");
		String queueName = channel.queueDeclare().getQueue();

		for (String bindingKey : bindingKeys) {
			channel.queueBind(queueName, topic, bindingKey);
			logger.debug("Binding to queue name: " + queueName + ", topic: " + topic + ", binding key: " + bindingKey);
		}

		logger.debug("Waiting for messages");

		QueueingConsumer consumer = new QueueingConsumer(channel);
		channel.basicConsume(queueName, true, consumer);

		QueueingConsumer.Delivery delivery = consumer.nextDelivery();

		String message = new String(delivery.getBody());
		String routingKey = delivery.getEnvelope().getRoutingKey();
		logger.info("Received '" + routingKey + "':'" + message + "'");

		return message;
	}
}
