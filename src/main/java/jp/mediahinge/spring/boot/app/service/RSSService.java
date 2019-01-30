package jp.mediahinge.spring.boot.app.service;

import java.io.IOException;
import java.util.Collection;
import java.util.List;

import org.springframework.stereotype.Service;

import com.cloudant.client.api.query.*;
import com.cloudant.client.api.query.QueryBuilder;

import jp.mediahinge.spring.boot.app.bean.RSSBean;

@Service
public class RSSService extends CloudantService{

	public Collection<RSSBean> getAll(){
		List<RSSBean> docs;
		try {
			docs = getDB().getAllDocsRequestBuilder().includeDocs(true).build().getResponse().getDocsAs(RSSBean.class);
		} catch (IOException e) {
			return null;
		}
		return docs;
	}

	public RSSBean get(String id) {
		return getDB().find(RSSBean.class, id);
	}

	/**
	 * Search url from rss list.
	 * 
	 * @return search results
	 */
	public List<RSSBean> searchURL(String url) {
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
		System.out.println("DEBUG:" + getDB().query(selector, RSSBean.class));
		QueryResult queryResult = getDB().query(selector, RSSBean.class);
		System.out.println("DEBUG:" + queryResult.getDocs());
		List<RSSBean> rssList = queryResult.getDocs();
		return rssList;
	}

	public RSSBean persist(RSSBean RSSForm) {
		String id = getDB().save(RSSForm).getId();
		return getDB().find(RSSBean.class, id);
	}

	public RSSBean update(String id, RSSBean newRSSForm) {
		RSSBean RSSForm = getDB().find(RSSBean.class, id);
		RSSForm.setUrl(newRSSForm.getUrl());
		getDB().update(RSSForm);
		return getDB().find(RSSBean.class, id);

	}

	public void delete(String id) {
		RSSBean RSSForm = getDB().find(RSSBean.class, id);
		getDB().remove(id, RSSForm.get_rev());

	}

	public int count() throws Exception {
		return getAll().size();
	}

}
