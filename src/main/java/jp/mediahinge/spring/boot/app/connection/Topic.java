package jp.mediahinge.spring.boot.app.connection;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import com.ibm.watson.developer_cloud.natural_language_understanding.v1.model.ConceptsResult;

import jp.mediahinge.spring.boot.app.form.ArticleForm;
import jp.mediahinge.spring.boot.app.form.HighestScoreTopic;
import jp.mediahinge.spring.boot.app.form.Score;
import jp.mediahinge.spring.boot.app.form.TopicForm;
import jp.mediahinge.spring.boot.app.schedule.ScheduledMethods;
import jp.mediahinge.spring.boot.app.service.TopicService;

public class Topic {

	private List<String> tags = new ArrayList<>();
	
	public List<TopicForm> getPastTopics(TopicService topicService){

		Calendar calendar = Calendar.getInstance();
		Date today = new Date();
		calendar.setTime(today);
		calendar.add(Calendar.DATE, -2);
		Date yes_yesterday = calendar.getTime();
		SimpleDateFormat id_format = new SimpleDateFormat("yyyyMMdd");
		String yes_yesterday_topic = id_format.format(yes_yesterday) + "00";
		
		return topicService.getPastTopics(Integer.parseInt(yes_yesterday_topic));
	}
	
	public void updateTopic(TopicService topicService, HighestScoreTopic highestScoreTopic) {
		TopicForm topicForm = highestScoreTopic.getTopicForm();
		List<Score> scores = topicForm.getArticle_list();
		
		
		
		for(Object obj : scores) {
			
			Score score = (Score) obj;
			
//			if(score.getMedia().equals(highestScoreTopic.getMedia())) {
//				if(score.getScore() < highestScoreTopic.getScore()) {
//					score.setArticle_id(article_id);
//					
//					topicService.updateTopics_id(topicForm.get_id(), topicForm);
//				}
//			}
		}
	}

	public void createTopic(TopicService topicService, ArticleForm articleForm, List<ConceptsResult> concepts) {
		TopicForm topicForm = new TopicForm();
		topicForm.setType("topic");
		int topic_id = ScheduledMethods.getTopic_id();
		Date today = new Date();
		SimpleDateFormat id_format = new SimpleDateFormat("yyyyMMdd");
		if(topic_id < 10) {
			topicForm.setTopic_id(Integer.parseInt(id_format.format(today) + "0" + topic_id));
		} else {
			topicForm.setTopic_id(Integer.parseInt(id_format.format(today) + topic_id));
		}
		
		List<Score> scores = new ArrayList<>();
		Score score = new Score();
		score.setArticle_id(articleForm.get_id());
		score.setMedia(articleForm.getMedia());
		score.setScore(100);
		scores.add(score);
		topicForm.setArticle_list(scores);
		
		for(Object obj : concepts) {
			ConceptsResult concept = (ConceptsResult) obj;
			this.addTag(concept);
		}
		topicForm.setTags(tags);
		
		topicService.persist(topicForm);
	}

	/**
	 * relevanceが0.75以上のconceptをタグとしてタグリストに追加
	 * 
	 * @return List<String> tags 
	 */
	public void addTag(ConceptsResult concept) {
		if(concept.getRelevance() >= 0.7) {
			tags.add(concept.getText());
		}
	}
}
