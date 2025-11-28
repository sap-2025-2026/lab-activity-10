package sap.kafka.sync;

import java.util.Collections;
import java.util.Properties;

import org.apache.kafka.clients.admin.Admin;
import org.apache.kafka.clients.admin.AdminClientConfig;

public class EventChannelsAdmin {

	private Admin adminClient;
	
	public EventChannelsAdmin(String address) {
		Properties config = new Properties();
		config.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, address);
		adminClient = Admin.create(config);
	}
	
	public void purgeChannel(String channelName) {	
		adminClient.deleteTopics(Collections.singletonList(channelName));
	}
	
}
