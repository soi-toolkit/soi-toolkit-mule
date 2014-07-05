package se.skltp.aggregatingservices.riv.crm.requeststatus.getrequestactivities;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.soitoolkit.commons.mule.jaxb.JaxbUtil;

import se.riv.crm.requeststatus.getrequestactivitiesresponder.v1.GetRequestActivitiesResponseType;
import se.riv.crm.requeststatus.getrequestactivitiesresponder.v1.ObjectFactory;
import se.skltp.agp.riv.interoperability.headers.v1.ProcessingStatusType;
import se.skltp.agp.service.api.QueryObject;
import se.skltp.agp.service.api.ResponseListFactory;

public class ResponseListFactoryImpl implements ResponseListFactory {

	private static final Logger log = LoggerFactory.getLogger(ResponseListFactoryImpl.class);
	private static final JaxbUtil jaxbUtil = new JaxbUtil(GetRequestActivitiesResponseType.class, ProcessingStatusType.class);
	private static final ObjectFactory OF = new ObjectFactory();
	
	@Override
	public String getXmlFromAggregatedResponse(QueryObject queryObject, List<Object> aggregatedResponseList) {
		GetRequestActivitiesResponseType aggregatedResponse = new GetRequestActivitiesResponseType();

	    for (Object object : aggregatedResponseList) {
	    	GetRequestActivitiesResponseType response = (GetRequestActivitiesResponseType)object;
			aggregatedResponse.getRequestActivity().addAll(response.getRequestActivity());
		}

	    if (log.isInfoEnabled()) {
    		String subjectOfCareId = queryObject.getFindContent().getRegisteredResidentIdentification();
        	log.info("Returning {} aggregated remisstatus for subject of care id {}", aggregatedResponse.getRequestActivity().size() ,subjectOfCareId);
        }
        
        // Since the class GetRequestActivitiesResponseType don't have an @XmlRootElement annotation
        // we need to use the ObjectFactory to add it.
        return jaxbUtil.marshal(OF.createGetRequestActivitiesResponse(aggregatedResponse));
	}
}