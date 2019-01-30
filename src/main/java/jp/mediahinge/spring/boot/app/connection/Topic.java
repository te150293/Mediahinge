package jp.mediahinge.spring.boot.app.connection;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import com.ibm.watson.developer_cloud.natural_language_understanding.v1.model.ConceptsResult;
import com.ibm.watson.developer_cloud.natural_language_understanding.v1.model.EntitiesResult;

import jp.mediahinge.spring.boot.app.bean.ArticleBean;
import jp.mediahinge.spring.boot.app.bean.Score;
import jp.mediahinge.spring.boot.app.bean.TopicBean;
import jp.mediahinge.spring.boot.app.schedule.ScheduledMethods;
import jp.mediahinge.spring.boot.app.service.TopicService;
import jp.mediahinge.spring.boot.app.temp_class.HighestScoreTopic;

public class Topic {

	private List<String> tags = new ArrayList<>();
	
	public List<TopicBean> getPastTopics(TopicService topicService){

		Calendar calendar = Calendar.getInstance();
		Date today = new Date();
		calendar.setTime(today);
		calendar.add(Calendar.DATE, -2);
		Date yes_yesterday = calendar.getTime();
		SimpleDateFormat id_format = new SimpleDateFormat("yyyyMMdd");
		String yes_yesterday_topic = id_format.format(yes_yesterday) + "00";
		
		return topicService.getRecentTopics(Integer.parseInt(yes_yesterday_topic));
	}
	
	public void updateTopic(TopicService topicService, HighestScoreTopic highestScoreTopic/* ScoreTopic scoreTopic */, List<ConceptsResult> concepts, List<EntitiesResult> tempEntities/*, スコア*/) {
		TopicBean topicForm = highestScoreTopic.getTopicForm();
		List<Score> scores = topicForm.getArticle_list();
		
		Score highestScore = highestScoreTopic.getScore();
		
		boolean existed = false;
		
		int i = 0;
		for(Object obj : scores) {
			Score score = (Score) obj;
			if(score.getMedia().equals(highestScore.getMedia())) {
				existed = true;
				if(score.getScore() < highestScore.getScore()) {
					scores.set(i, highestScore);
				}
			}
			i++;
		}
		if(!existed) {
			scores.add(highestScore);
		}
		tags = topicForm.getTags();
		this.addTag(concepts);
		
		topicForm.setArticle_list(scores);
		topicService.updateTopics_id(topicForm.get_id(), topicForm);
		System.out.println("Successfully updated Topic!\n");
		
	}

	public void createTopic(TopicService topicService, ArticleBean articleForm, List<ConceptsResult> concepts, List<EntitiesResult> entities) {
		TopicBean topicForm = new TopicBean();
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
		
		this.addTag(concepts);
		topicForm.setTags(tags);
		
		topicService.persist(topicForm);
		System.out.println("Successfully created Topic!\n");
	}

	/**
	 * relevanceが0.72以上のconceptをタグとしてタグリストに追加
	 *  
	 */
	public void addTag(List<ConceptsResult> concepts) {
		for(Object obj : concepts) {
			ConceptsResult concept = (ConceptsResult) obj;
			if(concept.getRelevance() >= 0.72) {
				if(!Arrays.asList(tags).contains(concept.getText())) {
					tags.add(concept.getText());
				}
			}
		}
	}
}
