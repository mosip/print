package io.mosip.print.init;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

import io.mosip.kernel.core.websub.spi.SubscriptionClient;
import io.mosip.kernel.websub.api.model.SubscriptionChangeRequest;
import io.mosip.kernel.websub.api.model.SubscriptionChangeResponse;
import io.mosip.kernel.websub.api.model.UnsubscriptionRequest;

@Component
public class SetupPrint 
implements ApplicationListener<ApplicationReadyEvent> {

    @Autowired
  SubscriptionClient<SubscriptionChangeRequest,UnsubscriptionRequest, SubscriptionChangeResponse> sb; 
  
  @Value("${mosip.event.hubURL}")   
  private String hubURL;
  
  @Value("${mosip.event.topic}")
  private String topic;
 

  @Value("${mosip.event.secret}")
  private String secret;
  
  //private String topicName="792112/CREDENTIAL_ISSUED";
  /**
   * This event is executed as late as conceivably possible to indicate that 
   * the application is ready to service requests.
   */
  
  @Override
  public void onApplicationEvent(final ApplicationReadyEvent event) {
    
    SubscriptionChangeRequest subscriptionChangeRequest = new SubscriptionChangeRequest();
    subscriptionChangeRequest.setHubURL(hubURL+"/hub");
    subscriptionChangeRequest.setTopic(topic);
    subscriptionChangeRequest.setSecret(secret);
    subscriptionChangeRequest.setCallbackURL("http://localhost:8098/print/callback/notifyPrint");
   // subscriptionChangeRequest.setLeaseSeconds(320);
    //subscriptionChangeRequest.setLeaseSeconds(leaseSeconds);
    //HttpHeaders httpHeaders=new HttpHeaders();
   // sb.publishUpdate(issuer+"/"+"CREDENTIAL_ISSUED", eventModel, MediaType.APPLICATION_JSON_UTF8_VALUE, httpHeaders,  hubURL);

    sb.subscribe(subscriptionChangeRequest);
    return;
  }
}