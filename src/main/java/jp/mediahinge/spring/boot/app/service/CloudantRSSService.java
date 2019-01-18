package jp.mediahinge.spring.boot.app.service;

import java.io.IOException;
import java.util.Collection;
import java.util.List;

import org.springframework.stereotype.Service;

import com.cloudant.client.api.query.*;
import com.cloudant.client.api.query.QueryBuilder;

import jp.mediahinge.spring.boot.app.form.RSSForm;

@Service
public class CloudantRSSService extends CloudantService{
    
    public Collection<RSSForm> getAll(){
        List<RSSForm> docs;
        try {
            docs = getDB().getAllDocsRequestBuilder().includeDocs(true).build().getResponse().getDocsAs(RSSForm.class);
        } catch (IOException e) {
            return null;
        }
        return docs;
    }

    public RSSForm get(String id) {
        return getDB().find(RSSForm.class, id);
    }
    
    /**
     * Search url from rss list.
     * 
     * @return search results
     */
    public List<RSSForm> searchURL(String url) {
    	String selector = 
        		"{\r\n" + 
        		"   \"selector\": {\r\n" + 
        		"      \"$and\": [\r\n" + 
        		"         {\r\n" + 
        		"            \"type\": {\r\n" + 
        		"               \"$eq\": \"rss\"\r\n" + 
        		"            },\r\n" + 
        		"            \"url\": {\r\n" + 
        		"               \"$eq\": \"" + url + "\"\r\n" + 
        		"            }\r\n" + 
        		"         }\r\n" + 
        		"      ]\r\n" + 
        		"   },\r\n" + 
//        		"   \"use_index\": \"_design/8615868e79c05bc73f85a1385c5f45a2352a056f\",\r\n" + 
        		"   \"fields\": [\r\n" + 
        		"      \"_id\",\r\n" + 
        		"      \"_rev\",\r\n" + 
        		"      \"type\",\r\n" + 
        		"      \"media\",\r\n" + 
        		"      \"url\"\r\n" + 
        		"   ]\r\n" + 
        		"}";
        	System.out.println("DEBUG:" + getDB().query(selector, RSSForm.class));
        	QueryResult queryResult = getDB().query(selector, RSSForm.class);
        	System.out.println("DEBUG:" + queryResult.getDocs());
        	List<RSSForm> rssList = queryResult.getDocs();
        	return rssList;
    }

    public RSSForm persist(RSSForm RSSForm) {
        String id = getDB().save(RSSForm).getId();
        return getDB().find(RSSForm.class, id);
    }

    public RSSForm update(String id, RSSForm newRSSForm) {
        RSSForm RSSForm = getDB().find(RSSForm.class, id);
        RSSForm.setUrl(newRSSForm.getUrl());
        getDB().update(RSSForm);
        return getDB().find(RSSForm.class, id);

    }

    public void delete(String id) {
        RSSForm RSSForm = getDB().find(RSSForm.class, id);
        getDB().remove(id, RSSForm.get_rev());

    }

    public int count() throws Exception {
        return getAll().size();
    }

}
