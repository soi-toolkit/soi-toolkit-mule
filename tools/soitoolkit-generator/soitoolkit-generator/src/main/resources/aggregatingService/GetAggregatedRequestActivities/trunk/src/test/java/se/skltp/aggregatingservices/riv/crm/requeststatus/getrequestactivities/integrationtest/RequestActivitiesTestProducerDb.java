package se.skltp.aggregatingservices.riv.crm.requeststatus.getrequestactivities.integrationtest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import se.riv.crm.requeststatus.getrequestactivitiesresponder.v1.GetRequestActivitiesResponseType;
import se.riv.crm.requeststatus.v1.RequestActivityType;
import se.skltp.agp.test.producer.TestProducerDb;

public class RequestActivitiesTestProducerDb extends TestProducerDb {

	private static final Logger log = LoggerFactory.getLogger(RequestActivitiesTestProducerDb.class);

	@Override
	public Object createResponse(Object... responseItems) {
		log.debug("Creates a response with {} items", responseItems);
		GetRequestActivitiesResponseType response = new GetRequestActivitiesResponseType();
		for (int i = 0; i < responseItems.length; i++) {
			response.getRequestActivity().add((RequestActivityType)responseItems[i]);
		}
		return response;
	}
	
	@Override
	public Object createResponseItem(String logicalAddress, String registeredResidentId, String businessObjectId, String time) {
		
		if (log.isDebugEnabled()) {
			log.debug("Created one response item for logical-address {}, registeredResidentId {} and businessObjectId {}",
				new Object[] {logicalAddress, registeredResidentId, businessObjectId});
		}

		RequestActivityType response = new RequestActivityType();

		response.setCareUnit(logicalAddress);
		response.setSubjectOfCareId(registeredResidentId);
		response.setSenderRequestId(businessObjectId);
		response.setReceiverRequestId("ReceiverRequestId");
		response.setTypeOfRequest("TypeOfRequest");
		response.setRequestMedium("RequestMedium");
		response.setRequestIssuedByPersonName("RequestIssuedByPersonName");
		response.setRequestIssuedByOrganizationalUnitId("RequestIssuedByOrganizationalUnitId");
		response.setRequestIssuedByOrganizationalUnitDescription("RequestIssuedByOrganizationalUnitDescription");
		response.setReceivingPersonName("ReceivingPersonName");
		response.setReceivingOrganizationalUnitId("ReceivingOrganizationalUnitId");
		response.setReceivingOrganizationalUnitDescription("ReceivingOrganizationalUnitDescription");
		
		return response;
	}
}