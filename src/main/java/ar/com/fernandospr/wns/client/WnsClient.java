package ar.com.fernandospr.wns.client;

import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;

import org.apache.commons.httpclient.auth.AuthScope;
import org.codehaus.jackson.jaxrs.JacksonJsonProvider;

import ar.com.fernandospr.wns.exceptions.WnsException;
import ar.com.fernandospr.wns.model.WnsAbstractNotification;
import ar.com.fernandospr.wns.model.WnsNotificationRequestOptional;
import ar.com.fernandospr.wns.model.WnsNotificationResponse;
import ar.com.fernandospr.wns.model.WnsOAuthToken;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.WebResource.Builder;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;
import com.sun.jersey.api.client.filter.LoggingFilter;
import com.sun.jersey.core.util.MultivaluedMapImpl;
import com.sun.jersey.client.apache.ApacheHttpClient;
import com.sun.jersey.client.apache.config.DefaultApacheHttpClientConfig;

public class WnsClient {
	private static final String SCOPE = "notify.windows.com";
	private static final String GRANT_TYPE_CLIENT_CREDENTIALS = "client_credentials";
	private static final String AUTHENTICATION_URI = "https://login.live.com/accesstoken.srf";
	
	private String sid;
	private String clientSecret;
	private WnsOAuthToken token;
	private Client client;
	
	public WnsClient(String sid, String clientSecret, boolean logging) {
		this.sid = sid;
		this.clientSecret = clientSecret;
		this.client = createClient(logging);
	}
	
	private static Client createClient(boolean logging) {
		
		// from https://wikis.oracle.com/pages/viewpage.action?pageId=16023606
		final String proxyHost = System.getProperty("https.proxyHost", "");
    	final String proxyPort = System.getProperty("https.proxyPort", "3128");
         
        final DefaultApacheHttpClientConfig config = new DefaultApacheHttpClientConfig();
        if( ! proxyHost.trim().isEmpty() ){
            config.getProperties().put(DefaultApacheHttpClientConfig.PROPERTY_PROXY_URI, "http://" + proxyHost + ":" + proxyPort);
             
            final String proxyUser = System.getProperty("https.proxyUser","");
            final String proxyPassword = System.getProperty("https.proxyPassword","");
            if( ! proxyUser.trim().isEmpty() ){
                config.getState().setProxyCredentials(AuthScope.ANY_REALM, proxyHost, Integer.parseInt(proxyPort), proxyUser, proxyPassword);
            }
        }
		config.getClasses().add(JacksonJsonProvider.class);
		Client client = ApacheHttpClient.create(config);

		if(logging){
			client.addFilter(new LoggingFilter(System.out));
		}

		return client;
	}
	
	/**
	 * Based on <a href="http://msdn.microsoft.com/en-us/library/windows/apps/hh465407.aspx">http://msdn.microsoft.com/en-us/library/windows/apps/hh465407.aspx</a>
	 * @throws WnsException when authentication fails
	 */
	public void refreshAccessToken() throws WnsException {
		WebResource webResource = this.client.resource(AUTHENTICATION_URI);
		MultivaluedMap<String, String> formData = new MultivaluedMapImpl();
		formData.add("grant_type", GRANT_TYPE_CLIENT_CREDENTIALS);
		formData.add("client_id", this.sid);
		formData.add("client_secret", this.clientSecret);
		formData.add("scope", SCOPE);
		ClientResponse response = webResource.type(MediaType.APPLICATION_FORM_URLENCODED_TYPE)
											 .accept(MediaType.APPLICATION_JSON_TYPE)
											 .post(ClientResponse.class, formData);
		if (response.getStatus() != 200) {
			throw new WnsException("Authentication failed. HTTP error code: " + response.getStatus());
		}
		
		this.token = response.getEntity(WnsOAuthToken.class);
	}
	
	/**
	 * @param channelUri
	 * @param resourceBuilder
	 * @param notification
	 * @param retriesLeft to push the notification if the token expires
	 * @return WnsNotificationResponse please see response headers from <a href="http://msdn.microsoft.com/en-us/library/windows/apps/hh465435.aspx#send_notification_response">http://msdn.microsoft.com/en-us/library/windows/apps/hh465435.aspx#send_notification_response</a>
	 * @throws WnsException when authentication fails
	 */
	public WnsNotificationResponse push(WnsResourceBuilder resourceBuilder, String channelUri, WnsAbstractNotification notification, int retriesLeft, WnsNotificationRequestOptional optional) throws WnsException {
		WebResource webResource = this.client.resource(channelUri);
		
		Builder webResourceBuilder = resourceBuilder.build(webResource, notification, getToken().access_token, optional);
		
		ClientResponse response = webResourceBuilder.post(ClientResponse.class);
		
		WnsNotificationResponse notificationResponse = new WnsNotificationResponse(channelUri, response.getStatus(), response.getHeaders());
		if (notificationResponse.code == 200) {
			return notificationResponse;
		}
		
		if (notificationResponse.code == 401 && retriesLeft > 0) {
			retriesLeft--;
			// Access token may have expired
			refreshAccessToken();
			// Retry
			return this.push(resourceBuilder, channelUri, notification, retriesLeft, optional);
		}
		
		// Assuming push failed
		return notificationResponse;
	}
	
	private WnsOAuthToken getToken() throws WnsException {
		if (this.token == null) {
			refreshAccessToken();
		}
		return this.token;
	}

	/**
	 * @param channelUris
	 * @param resourceBuilder
	 * @param notification
	 * @param retriesLeft to push the notification if the token expires
	 * @return list of WnsNotificationResponse for each channelUri, please see response headers from <a href="http://msdn.microsoft.com/en-us/library/windows/apps/hh465435.aspx#send_notification_response">http://msdn.microsoft.com/en-us/library/windows/apps/hh465435.aspx#send_notification_response</a>
	 * @throws WnsException when authentication fails
	 */
	public List<WnsNotificationResponse> push(WnsResourceBuilder resourceBuilder, List<String> channelUris, WnsAbstractNotification notification, int retriesLeft, WnsNotificationRequestOptional optional) throws WnsException {
		List<WnsNotificationResponse> responses = new ArrayList<WnsNotificationResponse>();
		for (String channelUri : channelUris) {
			WnsNotificationResponse response = push(resourceBuilder, channelUri, notification, retriesLeft, optional);
			responses.add(response);
		}
		return responses;
	}
}
