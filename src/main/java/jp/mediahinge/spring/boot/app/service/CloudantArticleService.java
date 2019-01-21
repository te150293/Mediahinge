package jp.mediahinge.spring.boot.app.service;

import java.io.IOException;
import java.util.Collection;
import java.util.List;

import org.springframework.stereotype.Service;

import com.cloudant.client.api.query.QueryResult;

import jp.mediahinge.spring.boot.app.form.ArticleForm;
import jp.mediahinge.spring.boot.app.form.RSSForm;

@Service
public class CloudantArticleService extends CloudantService{

	public Collection<ArticleForm> getAll(){
		List<ArticleForm> docs;
		try {
			docs = getDB().getAllDocsRequestBuilder().includeDocs(true).build().getResponse().getDocsAs(ArticleForm.class);
		} catch (IOException e) {
			return null;
		}
		return docs;
	}

	public ArticleForm get(String id) {
		return getDB().find(ArticleForm.class, id);
	}

	public ArticleForm persist(ArticleForm articleForm) {
		String id = getDB().save(articleForm).getId();
		return getDB().find(ArticleForm.class, id);
	}

	public ArticleForm updateTopics_id(String id, ArticleForm newArticleForm) {
		ArticleForm articleForm = getDB().find(ArticleForm.class, id);
		articleForm.setTopics_id(newArticleForm.getTopics_id());
		getDB().update(articleForm);
		return getDB().find(ArticleForm.class, id);

	}

	public void delete(String id) {
		ArticleForm articleForm = getDB().find(ArticleForm.class, id);
		getDB().remove(id, articleForm.get_rev());

	}

	public int count() throws Exception {
		return getAll().size();
	}


	/**
	 * Get article topics_id = -1.
	 * 
	 * @return search results
	 */
	public List<ArticleForm> getNotGroupedArticle() {String selector = 
			"{\r\n" + 
			"   \"selector\": {\r\n" + 
			"      \"$and\": [\r\n" + 
			"         {\r\n" + 
			"            \"type\": {\r\n" + 
			"               \"$eq\": \"article\"\r\n" + 
			"            },\r\n" + 
			"            \"topics_id\": {\r\n" + 
			"               \"$eq\": -1\r\n" + 
			"            }\r\n" + 
			"         }\r\n" + 
			"      ]\r\n" + 
			"   },\r\n" + 
			"   \"fields\": [\r\n" + 
			"      \"_id\",\r\n" + 
			"      \"_rev\",\r\n" + 
			"      \"type\",\r\n" + 
			"      \"heading\"\r\n" + 
			"   ],\r\n" + 
			"   \"limit\": 20\r\n" + 
			"}";
	System.out.println("DEBUG:" + getDB().query(selector, RSSForm.class));
	QueryResult queryResult = getDB().query(selector, RSSForm.class);
	System.out.println("DEBUG:" + queryResult.getDocs());
	List<ArticleForm> articleList = queryResult.getDocs();
	return articleList;

	}
}
