package jp.mediahinge.spring.boot.app.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import org.springframework.stereotype.Service;

import com.cloudant.client.api.query.QueryResult;

import jp.mediahinge.spring.boot.app.form.ArticleForm;
import jp.mediahinge.spring.boot.app.form.TopicForm;
import jp.mediahinge.spring.boot.app.form.TopicForm;

@Service
public class TopicService extends CloudantService{

	public Collection<TopicForm> getAll(){
		List<TopicForm> docs;
		try {
			docs = getDB().getAllDocsRequestBuilder().includeDocs(true).build().getResponse().getDocsAs(TopicForm.class);
		} catch (IOException e) {
			return null;
		}
		return docs;
	}

	public TopicForm get(String id) {
		return getDB().find(TopicForm.class, id);
	}

	public TopicForm persist(TopicForm topicForm) {
		String id = getDB().save(topicForm).getId();
		return getDB().find(TopicForm.class, id);
	}
	

	public TopicForm updateTopics_id(String id, TopicForm newTopicForm) {
		TopicForm topicForm = getDB().find(TopicForm.class, id);
		topicForm.setArticle_list(newTopicForm.getArticle_list());
		getDB().update(topicForm);
		return getDB().find(TopicForm.class, id);
	}

	public void delete(String id) {
		TopicForm TopicForm = getDB().find(TopicForm.class, id);
		getDB().remove(id, TopicForm.get_rev());

	}

	public int count() throws Exception {
		return getAll().size();
	}
	
	public List<TopicForm> getPastTopics(int yes_yesterday){
		List<String> fields = new ArrayList<>();
		fields.add("topic_id");
		fields.add("article_list");
		fields.add("tags");
		
		String selector = Selector.buildSelector("topic", "topic_id", "gte", yes_yesterday, fields);

		QueryResult queryResult = getDB().query(selector, TopicForm.class);
		List<TopicForm> topicList = queryResult.getDocs();
		return topicList;
	}

	public TopicForm searchByTopic_id(int topic_id){
		List<String> fields = new ArrayList<>();
		fields.add("topic_id");
		fields.add("article_list");
		fields.add("tags");
		String selector = Selector.buildSelector("topic", "topic_id", "eq", topic_id, fields);

		QueryResult queryResult = getDB().query(selector, TopicForm.class);
		List<TopicForm> topicList = queryResult.getDocs();
		if(!topicList.isEmpty()) {
			return topicList.get(0);
		}else {
			return null;
		}
	}

}
