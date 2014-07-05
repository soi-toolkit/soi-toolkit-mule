package se.skltp.aggregatingservices.riv.crm.requeststatus.getrequestactivities;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Test;
import org.junit.Ignore;

import se.skltp.agp.riv.itintegration.engagementindex.findcontentresponder.v1.FindContentResponseType;
import se.skltp.agp.service.api.QueryObject;
import se.skltp.agp.service.api.RequestListFactory;

public class RequestListFactoryTest {
	
	private static final String RONTGEN = "1";
	private static final String LABB = "2";
	private static final String ALLMAN = "4";
	private static final String FYSIOLOG = "10";

	private RequestListFactory testObject = new RequestListFactoryImpl();
	
	@Test
	@Ignore
	public void testQueryObjectFactory() {
		
		QueryObject qo = null;
		FindContentResponseType src = null;
		testObject.createRequestList(qo, src);
		assertEquals("expected", "actual");
	}
	
	@Test
	public void nullCategoryMeansAllCategoriesAreCorrect(){
		boolean result = new RequestListFactoryImpl().isCorrectCategory(null, RONTGEN);
		assertTrue(result);
	}
	
	@Test
	public void emptyCategoryMeansAllCategoriesAreCorrect(){
		List<String> reqTypeOfRequestList = new ArrayList<String>();
		boolean result = new RequestListFactoryImpl().isCorrectCategory(reqTypeOfRequestList, RONTGEN);
		assertTrue(result);
	}
	
	@Test
	public void whenExactCorrectCategoryReturnTrue(){
		List<String> reqTypeOfRequestList = Arrays.asList(RONTGEN);
		boolean result = new RequestListFactoryImpl().isCorrectCategory(reqTypeOfRequestList, RONTGEN);
		assertTrue(result);
	}
	
	@Test
	public void atLeastOneCorrectCategoryReturnTrue(){
		List<String> reqTypeOfRequestList = Arrays.asList(RONTGEN,LABB,ALLMAN,FYSIOLOG);
		boolean result = new RequestListFactoryImpl().isCorrectCategory(reqTypeOfRequestList, RONTGEN);
		assertTrue(result);
	}
	
	@Test
	public void whenNoExactCorrectCategoryReturnFalse(){
		List<String> reqTypeOfRequestList = Arrays.asList(LABB);
		boolean result = new RequestListFactoryImpl().isCorrectCategory(reqTypeOfRequestList, RONTGEN);
		assertFalse(result);
	}
	
	@Test
	public void whenNoCorrectCategoryInListReturnFalse(){
		List<String> reqTypeOfRequestList = Arrays.asList(LABB,ALLMAN,FYSIOLOG);
		boolean result = new RequestListFactoryImpl().isCorrectCategory(reqTypeOfRequestList, RONTGEN);
		assertFalse(result);
	}
	
	@Test
	public void isPartOf(){
		List<String> careUnitIdList = Arrays.asList("UNIT1","UNIT2");
		assertTrue(new RequestListFactoryImpl().isPartOf(careUnitIdList, "UNIT2"));
		assertTrue(new RequestListFactoryImpl().isPartOf(careUnitIdList, "UNIT1"));
		
		careUnitIdList = new ArrayList<String>();
		assertTrue(new RequestListFactoryImpl().isPartOf(careUnitIdList, "UNIT1"));
		
		careUnitIdList = null;
		assertTrue(new RequestListFactoryImpl().isPartOf(careUnitIdList, "UNIT1"));
	}
	
	@Test
	public void isNotPartOf(){
		List<String> careUnitIdList = Arrays.asList("UNIT1","UNIT2");
		assertFalse(new RequestListFactoryImpl().isPartOf(careUnitIdList, "UNIT3"));
		assertFalse(new RequestListFactoryImpl().isPartOf(careUnitIdList, null));
	}
	
}
