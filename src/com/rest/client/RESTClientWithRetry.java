package com.executor.service;

import java.net.SocketTimeoutException;
import javax.ws.rs.ProcessingException;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.glassfish.jersey.client.ClientProperties;


/**
 * The Class JerseyRestClient.
 */
public class RESTClientWithRetry {

	/** The instance. */
	private static JerseyRestClient instance;

	/** The client. */
	private Client client;

	/**
	 * Instantiates a new jersey rest client.
	 */
	private JerseyRestClient() {	
		if(client == null) {
			createClient();
		}
	}
	
	/**
	 * Creates the client.
	 */
	public void createClient() {
		client = ClientBuilder.newClient();
		client.property(ClientProperties.CONNECT_TIMEOUT, 3000);
		client.property(ClientProperties.READ_TIMEOUT,    3000);
	}

	/**
	 * Gets the single instance of JerseyRestClient.
	 *
	 * @return single instance of JerseyRestClient
	 */
	public static synchronized JerseyRestClient getInstance() {
		return instance == null ? new JerseyRestClient() : instance;
	}

	/**
	 * Gets the response as string.
	 *
	 * @param resourceLocation the resource location
	 * @return the response as string
	 */
	public String getResponseAsString(String resourceLocation)  {
		String output = null;

		try {
			WebTarget target = client.target(resourceLocation);
			
			int i = 0;
			int retryNumber = 2;
			
			Response res = null;
			
			while (true) {
			    try {
			    	res = target.request().accept(MediaType.APPLICATION_JSON).get();
			        break;
			    } catch (ProcessingException e){ // retry in case of exception
			        if (e.getCause() instanceof SocketTimeoutException && i < retryNumber) {
			            i++;
			        } else {
			            break;
			        }
			    }
			}

			if(res.getStatus() == 200) {
				output = res.readEntity(String.class);
			}
			res.close();

		} catch (Exception e) {
			e.printStackTrace();
		}
		return output;
	}
}
