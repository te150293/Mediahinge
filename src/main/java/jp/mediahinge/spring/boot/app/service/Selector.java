package jp.mediahinge.spring.boot.app.service;

import java.util.List;

public class Selector {
	
	public static String buildSelector(String type, String key, String operator, String value, List<String> fields) {
		String selector = 
				"{\r\n" + 
				"   \"selector\": {\r\n" + 
				"      \"$and\": [\r\n" + 
				"         {\r\n" + 
				"            \"type\": {\r\n" + 
				"               \"$eq\": \"" + type + "\"\r\n" + 
				"            },\r\n" + 
				"            \"" + key + "\": {\r\n" + 
				"               \"$" + operator +  "\": \"" + value + "\"\r\n" + 
				"            }\r\n" + 
				"         }\r\n" + 
				"      ]\r\n" + 
				"   },\r\n" + 
				"   \"fields\": [\r\n" + 
				"      \"_id\",\r\n" + 
				"      \"_rev\"";

		for(Object obj : fields) {
			String field = (String) obj;
			selector += ",\r\n "
					+ "      \"" + field + "\"";
		}
		
		selector += "\r\n "
				+ "   ]\r\n"
				+ "}";
		
		return selector;
	}

	public static String buildSelector(String type, String key, String operator, int value, List<String> fields) {
		String selector = 
				"{\r\n" + 
				"   \"selector\": {\r\n" + 
				"      \"$and\": [\r\n" + 
				"         {\r\n" + 
				"            \"type\": {\r\n" + 
				"               \"$eq\": \"" + type + "\"\r\n" + 
				"            },\r\n" + 
				"            \"" + key + "\": {\r\n" + 
				"               \"$" + operator +  "\": " + value + "\r\n" + 
				"            }\r\n" + 
				"         }\r\n" + 
				"      ]\r\n" + 
				"   },\r\n" + 
				"   \"fields\": [\r\n" + 
				"      \"_id\",\r\n" + 
				"      \"_rev\"";
		
		for(Object obj : fields) {
			String field = (String) obj;
			selector += ",\r\n "
					+ "      \"" + field + "\"";
		}
		
		selector += "\r\n "
				+ "   ]\r\n"
				+ "}";
		
		return selector;
	}

	public static String buildSelector(String type, String key, String operator, int value, List<String> fields, String sort, String order) {
		String selector = 
				"{\r\n" + 
				"   \"selector\": {\r\n" + 
				"      \"$and\": [\r\n" + 
				"         {\r\n" + 
				"            \"type\": {\r\n" + 
				"               \"$eq\": \"" + type + "\"\r\n" + 
				"            },\r\n" + 
				"            \"" + key + "\": {\r\n" + 
				"               \"$" + operator +  "\": " + value + "\r\n" + 
				"            }\r\n" + 
				"         }\r\n" + 
				"      ]\r\n" + 
				"   },\r\n" + 
				"   \"fields\": [\r\n" + 
				"      \"_id\",\r\n" + 
				"      \"_rev\"";
		
		for(Object obj : fields) {
			String field = (String) obj;
			selector += ",\r\n "
					+ "      \"" + field + "\"";
		}
		
		selector += 
				"\r\n " + 
				"   ]";
		
		selector += 
				",\r\n" +
				"   \"sort\": [\r\n" +
				"      {\r\n" +
				"         \"" + sort + "\": \"" + order + "\"\r\n" +
				"      }\r\n" +
				"   ],\r\n" +
				"   \"limit\": 200\r\n" +
				"}";
		
		return selector;
	}
}
