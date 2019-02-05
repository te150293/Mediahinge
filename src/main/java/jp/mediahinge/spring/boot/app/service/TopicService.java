package jp.mediahinge.spring.boot.app.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import org.springframework.stereotype.Service;

import com.cloudant.client.api.query.QueryResult;

import jp.mediahinge.spring.boot.app.bean.ArticleBean;
import jp.mediahinge.spring.boot.app.bean.TopicBean;

@Service
public class TopicService extends CloudantService{

	public Collection<TopicBean> getAll(){
		List<TopicBean> docs;
		try {
			docs = getDB().getAllDocsRequestBuilder().includeDocs(true).build().getResponse().getDocsAs(TopicBean.class);
		} catch (IOException e) {
			return null;
		}
		return docs;
	}

	public TopicBean get(String id) {
		return getDB().find(TopicBean.class, id);
	}

	public TopicBean persist(TopicBean topicForm) {
		String id = getDB().save(topicForm).getId();
		return getDB().find(TopicBean.class, id);
	}
	

	public TopicBean updateTopics_id(String id, TopicBean newTopicForm) {
		TopicBean topicForm = getDB().find(TopicBean.class, id);
		topicForm.setArticle_list(newTopicForm.getArticle_list());
		getDB().update(topicForm);
		return getDB().find(TopicBean.class, id);
	}

	public void delete(String id) {
		TopicBean TopicForm = getDB().find(TopicBean.class, id);
		getDB().remove(id, TopicForm.get_rev());

	}

	public int count() throws Exception {
		return getAll().size();
	}
	
	public List<TopicBean> getRecentTopics(int yes_yesterday){
		List<String> fields = new ArrayList<>();
		fields.add("topic_id");
		fields.add("article_list");
		fields.add("tags");
		
		String selector = Selector.buildSelector("topic", "topic_id", "gte", yes_yesterday, fields, "topic_id", "desc");

		System.out.println(selector);
		QueryResult queryResult = getDB().query(selector, TopicBean.class);
		List<TopicBean> topicList = queryResult.getDocs();
		return topicList;
	}

	public TopicBean searchByTopic_id(int topic_id){
		List<String> fields = new ArrayList<>();
		fields.add("topic_id");
		fields.add("article_list");
		fields.add("tags");
		String selector = Selector.buildSelector("topic", "topic_id", "eq", topic_id, fields);

		QueryResult queryResult = getDB().query(selector, TopicBean.class);
		List<TopicBean> topicList = queryResult.getDocs();
		if(!topicList.isEmpty()) {
			return topicList.get(0);
		}else {
			return null;
		}
	}
	
	public List<TopicBean> searchByTag(String tag, int skip){
		String selector = 
				"{\r\n" + 
				"   \"selector\": {\r\n" + 
				"      \"$and\": [\r\n" + 
				"         {\r\n" + 
				"            \"article_list\": {\r\n" + 
				"               \"$or\": [\r\n" + 
				"                  {\r\n" + 
				"                     \"$size\": 2\r\n" + 
				"                  },\r\n" + 
				"                  {\r\n" + 
				"                     \"$size\": 3\r\n" + 
				"                  }\r\n" + 
				"               ]\r\n" + 
				"            }\r\n" + 
				"         },\r\n" + 
				"         {\r\n" + 
				"            \"tags\": {\r\n" + 
				"               \"$elemMatch\": {\r\n" + 
				"                  \"$eq\": \"" + tag + "\"\r\n" + 
				"               }\r\n" + 
				"            }\r\n" + 
				"         }\r\n" + 
				"      ]\r\n" + 
				"   },\r\n" + 
				"   \"fields\": [\r\n" + 
				"      \"_id\",\r\n" + 
				"      \"_rev\",\r\n" + 
				"      \"topic_id\",\r\n" + 
				"      \"article_list\",\r\n" + 
				"      \"tags\"\r\n" + 
				"   ],\r\n" + 
				"   \"sort\": [\r\n" + 
				"      {\r\n" + 
				"         \"topic_id\": \"desc\"\r\n" + 
				"      }\r\n" + 
				"   ],\r\n" + 
				"   \"limit\": 10,\r\n" + 
				"   \"skip\": " + skip + "\r\n" + 
				"}";

		QueryResult queryResult = getDB().query(selector, TopicBean.class);
		List<TopicBean> topicList = queryResult.getDocs();
		return topicList;
	}
	
	public List<TopicBean> searchTopicByArticle_id(List<String> article_idList){
		String selector = 
				"{\r\n" + 
				"   \"selector\": {\r\n" + 
				"      \"article_list\": {\r\n" + 
				"         \"$elemMatch\": {\r\n" + 
				"            \"article_id\": {\r\n" + 
				"               \"$or\": [";
		
		for(int i = 0; i<article_idList.size();i++) {
			if(i == 0) {
				selector +=
						"\r\n" + 
						"                  {\r\n" + 
						"                     \"$eq\": \"" + article_idList.get(i) + "\"\r\n" + 
						"                  }";
			} else {
				selector +=
						",\r\n" + 
						"                  {\r\n" + 
						"                     \"$eq\": \"" + article_idList.get(i) + "\"\r\n" + 
						"                  }";
			}
		}
		
		selector +=
				"\r\n" + 
				"               ]\r\n" + 
				"            }\r\n" + 
				"         }\r\n" + 
				"      }\r\n" + 
				"   },\r\n" + 
				"   \"fields\": [\r\n" + 
				"      \"_id\",\r\n" + 
				"      \"_rev\"\r\n" + 
				"   ],\r\n" + 
				"   \"limit\": 100\r\n" + 
				"}";

		QueryResult queryResult = getDB().query(selector, TopicBean.class);
		List<TopicBean> topicList = queryResult.getDocs();
		return topicList;
	}
	
	public List<TopicBean> getSortedTopic(int skip){
		String selector =
				"{\r\n" + 
				"   \"selector\": {\r\n" + 
				"      \"$and\": [\r\n" + 
				"         {\r\n" + 
				"            \"article_list\": {\r\n" + 
				"               \"$or\": [\r\n" + 
				"                  {\r\n" + 
				"                     \"$size\": 2\r\n" + 
				"                  },\r\n" + 
				"                  {\r\n" + 
				"                     \"$size\": 3\r\n" + 
				"                  }\r\n" + 
				"               ]\r\n" + 
				"            }\r\n" + 
				"         }\r\n" + 
				"      ]\r\n" + 
				"   },\r\n" + 
				"   \"fields\": [\r\n" + 
				"      \"_id\",\r\n" + 
				"      \"_rev\",\r\n" + 
				"      \"topic_id\",\r\n" + 
				"      \"article_list\",\r\n" + 
				"      \"tags\"\r\n" + 
				"   ],\r\n" + 
				"   \"sort\": [\r\n" + 
				"      {\r\n" + 
				"         \"topic_id\": \"desc\"\r\n" + 
				"      }\r\n" + 
				"   ],\r\n" + 
				"   \"limit\": 10,\r\n" + 
				"   \"skip\": "+ skip + "\r\n" + 
				"}";

		QueryResult queryResult = getDB().query(selector, TopicBean.class);
		List<TopicBean> topicList = queryResult.getDocs();
		return topicList;
	}
}
