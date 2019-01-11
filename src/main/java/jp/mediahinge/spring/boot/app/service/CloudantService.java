package jp.mediahinge.spring.boot.app.service;

import java.net.URL;

import org.springframework.stereotype.Service;

import com.cloudant.client.api.ClientBuilder;
import com.cloudant.client.api.CloudantClient;
import com.cloudant.client.api.Database;
import com.google.gson.JsonObject;

@Service
public class CloudantService {

    private Database db = null;
    private static final String databaseName = "mediahinge_db02";
    
    public CloudantService(){
    	System.out.println("debug:instance");

    }
    
    public Database getDB(){
    	
    	if(db == null) {
	        CloudantClient cloudant = createClient();
	        if(cloudant!=null){
	         db = cloudant.database(databaseName, true);
	        }
    	}    	
        return db;
    }
    
    private static CloudantClient createClient() {
        
        String url;

        System.out.println("DEBUG0!!!!:" + System.getenv("VCAP_SERVICES") );
        if (System.getenv("VCAP_SERVICES") != null) {
            // When running in IBM Cloud, the VCAP_SERVICES env var will have the credentials for all bound/connected services
            // Parse the VCAP JSON structure looking for cloudant.
            JsonObject cloudantCredentials = VCAPHelper.getCloudCredentials("cloudant");
            if(cloudantCredentials == null){
                System.out.println("No cloudant database service bound to this application");
                return null;
            }
            url = cloudantCredentials.get("url").getAsString();
        } else if (System.getenv("CLOUDANT_URL") != null) {
            url = System.getenv("CLOUDANT_URL");
        } else {
            System.out.println("Running locally. Looking for credentials in cloudant.properties");
            url = VCAPHelper.getLocalProperties("cloudant.properties").getProperty("cloudant_url");
            if(url == null || url.length()==0){
                System.out.println("To use a database, set the Cloudant url in src/main/resources/cloudant.properties");
                return null;
            }
        }

        try {
            System.out.println("Connecting to Cloudant");
            CloudantClient client = ClientBuilder.url(new URL(url)).build();
//            CloudantClient client = ClientBuilder.url(new URL(url)).proxyURL(new URL("http://172.17.0.2:80")).build();
            System.out.println("Successfully connected to Cloudant!\n");
            return client;
        } catch (Exception e) {
            System.out.println("Unable to connect to database");
            //e.printStackTrace();
            return null;
        }
    }

    public void shutDown() {
    	this.createClient().shutdown();
    }
}
