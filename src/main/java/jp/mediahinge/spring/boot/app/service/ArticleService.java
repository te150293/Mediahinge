package jp.mediahinge.spring.boot.app.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.springframework.stereotype.Service;

import com.cloudant.client.api.query.QueryResult;

import jp.mediahinge.spring.boot.app.bean.ArticleBean;
import jp.mediahinge.spring.boot.app.bean.RSSBean;
import jp.mediahinge.spring.boot.app.bean.TopicBean;

@Service
public class ArticleService extends CloudantService{

	public Collection<ArticleBean> getAll(){
		List<ArticleBean> docs;
		try {
			docs = getDB().getAllDocsRequestBuilder().includeDocs(true).build().getResponse().getDocsAs(ArticleBean.class);
		} catch (IOException e) {
			return null;
		}
		return docs;
	}

	public ArticleBean get(String id) {
		return getDB().find(ArticleBean.class, id);
	}

	public ArticleBean persist(ArticleBean articleForm) {
		String id = getDB().save(articleForm).getId();
		return getDB().find(ArticleBean.class, id);
	}

	public ArticleBean updateTopics_id(String id, ArticleBean newArticleForm) {
		ArticleBean articleForm = getDB().find(ArticleBean.class, id);
		articleForm.setTopics_id(newArticleForm.getTopics_id());
		getDB().update(articleForm);
		return getDB().find(ArticleBean.class, id);

	}

	public void delete(String id) {
		ArticleBean articleForm = getDB().find(ArticleBean.class, id);
		getDB().remove(id, articleForm.get_rev());

	}

	public int count() throws Exception {
		return getAll().size();
	}

	/**
	 * Get article topic_id = -1.
	 * 
	 * @return search results
	 */
	public List<ArticleBean> getNotGroupedArticle() {
		String selector = 
			"{\r\n" + 
			"   \"selector\": {\r\n" + 
			"      \"$and\": [\r\n" + 
			"         {\r\n" + 
			"            \"type\": {\r\n" + 
			"               \"$eq\": \"article\"\r\n" + 
			"            },\r\n" + 
			"            \"topic_id\": {\r\n" + 
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
	System.out.println("DEBUG:" + getDB().query(selector, RSSBean.class));
	QueryResult queryResult = getDB().query(selector, RSSBean.class);
	System.out.println("DEBUG:" + queryResult.getDocs());
	List<ArticleBean> articleList = queryResult.getDocs();
	return articleList;

	}
	

	/**
	 * Get topic.
	 * 
	 * @return List<ArticleForm>.
	 */
	public List<ArticleBean> getArticlesSortedByTopics_id() {
		String selector = 
			"{\r\n" + 
			"   \"selector\": {\r\n" + 
			"      \"$and\": [\r\n" + 
			"         {\r\n" + 
			"            \"type\": {\r\n" + 
			"               \"$eq\": \"article\"\r\n" + 
			"            },\r\n" + 
			"            \"topics_id\": {\r\n" + 
			"               \"$ne\": -1\r\n" + 
			"            }\r\n" + 
			"         }\r\n" + 
			"      ]\r\n" + 
			"   },\r\n" + 
			"   \"fields\": [\r\n" + 
			"      \"_id\",\r\n" + 
			"      \"_rev\",\r\n" + 
			"      \"media\",\r\n" + 
			"      \"heading\",\r\n" + 
			"      \"first_paragraph\",\r\n" + 
			"      \"text\",\r\n" + 
			"      \"url\",\r\n" + 
			"      \"topics_id\"\r\n" + 
			"   ],\r\n" + 
			"   \"sort\": [\r\n" + 
			"      {\r\n" + 
			"         \"topics_id\": \"desc\"\r\n" + 
			"      }\r\n" + 
			"   ],\r\n" + 
			"   \"limit\": 30\r\n" + 
			"}";
		System.out.println("DEBUG:" + getDB().query(selector, ArticleBean.class));
		QueryResult queryResult = getDB().query(selector, ArticleBean.class);
		System.out.println("DEBUG:" + queryResult.getDocs());
		//resultsは検索結果
		List<ArticleBean> results = queryResult.getDocs();
		return results;
	}
	
	public List<ArticleBean> runQuery(String selector){
		System.out.println("DEBUG:" + getDB().query(selector, ArticleBean.class));
		QueryResult queryResult = getDB().query(selector, ArticleBean.class);
		System.out.println("DEBUG:" + queryResult.getDocs());
		//resultsは検索結果
		List<ArticleBean> results = queryResult.getDocs();
		return results;

	}
}
