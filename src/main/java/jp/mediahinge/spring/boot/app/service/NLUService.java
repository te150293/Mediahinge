package jp.mediahinge.spring.boot.app.service;

import java.io.IOException;
import java.util.Collection;
import java.util.List;

import org.springframework.stereotype.Service;

import com.cloudant.client.api.query.QueryResult;

import jp.mediahinge.spring.boot.app.form.ArticleForm;
import jp.mediahinge.spring.boot.app.form.NLUForm;
import jp.mediahinge.spring.boot.app.form.RSSForm;

@Service
public class NLUService extends CloudantService{

	public Collection<NLUForm> getAll(){
		List<NLUForm> docs;
		try {
			docs = getDB().getAllDocsRequestBuilder().includeDocs(true).build().getResponse().getDocsAs(NLUForm.class);
		} catch (IOException e) {
			return null;
		}
		return docs;
	}

	public NLUForm get(String id) {
		return getDB().find(NLUForm.class, id);
	}

	public NLUForm persist(NLUForm NLUForm) {
		String id = getDB().save(NLUForm).getId();
		return getDB().find(NLUForm.class, id);
	}

	public void delete(String id) {
		NLUForm NLUForm = getDB().find(NLUForm.class, id);
		getDB().remove(id, NLUForm.get_rev());

	}

	public int count() throws Exception {
		return getAll().size();
	}

    /**
	 * search AnalysisResults by concepts.
	 * 
	 * @return search results
	 */
	public List<NLUForm> searchByArticle_id(String article_id) {
		String selector = 
			"{\r\n" + 
			"   \"selector\": {\r\n" + 
			"      \"$and\": [\r\n" + 
			"         {\r\n" + 
			"            \"type\": {\r\n" + 
			"               \"$eq\": \"Analysis Results\"\r\n" + 
			"            },\r\n" + 
			"            \"article_id\": {\r\n" + 
			"               \"$eq\": \"" + article_id + "\"\r\n" + 
			"            }\r\n" + 
			"         }\r\n" + 
			"      ]\r\n" + 
			"   },\r\n" + 
			"   \"fields\": [\r\n" + 
			"      \"_id\",\r\n" + 
			"      \"_rev\",\r\n" + 
			"      \"article_id\",\r\n" + 
			"      \"concepts\",\r\n" + 
			"      \"keywords\",\r\n" + 
			"      \"results\"\r\n" + 
			"   ]\r\n" + 
			"}";
	QueryResult queryResult = getDB().query(selector, NLUForm.class);
	List<NLUForm> nluList = queryResult.getDocs();
	return nluList;
	}
}
