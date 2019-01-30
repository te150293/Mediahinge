package jp.mediahinge.spring.boot.app.processing;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.springframework.stereotype.Service;

import com.ibm.watson.developer_cloud.natural_language_understanding.v1.model.ConceptsResult;

import jp.mediahinge.spring.boot.app.bean.ArticleBean;
import jp.mediahinge.spring.boot.app.bean.NLUBean;
import jp.mediahinge.spring.boot.app.bean.TopicBean;
import jp.mediahinge.spring.boot.app.service.NLUService;

public class Topics {

	private List<TopicBean> topicList = new ArrayList();
	NLUService service = null;

	/**
	 * グループ化して自身のtopicListにセット
	 */
	public void grouping(List<ArticleBean> sortedResults, NLUService service){
		for(Object object : sortedResults) {
			ArticleBean article = (ArticleBean) object;

			//topicListが空だった場合
			if (topicList == null || topicList.isEmpty()) {
				this.addTopic(article, service);
				
				//topicListが空でなかった場合
			} else {
				//topicListの末尾を取得
				TopicBean lastTopic = topicList.get(topicList.size()-1);

				if(lastTopic.getTopic_id() == article.getTopics_id()) {
					this.updateLastTopic(article, service);
				} else {
					this.addTopic(article, service);
				}
			}
		}
	}

	public void addTopic(ArticleBean article, NLUService nluService) {
		List<String> list = new ArrayList();
		list.add(article.get_id());
		TopicBean topic = new TopicBean();
//		topic.setArticle_list(list);
		topic.setTopic_id(article.getTopics_id());
		
		List<NLUBean> analysisResults = nluService.searchByArticle_id(article.get_id());
		List<String> tags = new ArrayList();
		
		System.out.println("analysisResults:" + analysisResults + "\n");
		if(analysisResults.size() > 0) {
			topic = this.setTags(topic, analysisResults.get(0));
		}
		
		topicList.add(topic);
	}
	
	public void updateLastTopic(ArticleBean article, NLUService service) {
		TopicBean lastTopic = topicList.get(topicList.size()-1);
//		List<String> list = lastTopic.getArticle_list();
//		List<String> tags = lastTopic.getTags();
//		list.add(article.get_id());
//		lastTopic.setArticle_list(list);

		List<NLUBean> analysisResults = service.searchByArticle_id(article.get_id());
		
		System.out.println("analysisResults:" + analysisResults + "\n");
		if(analysisResults.size() > 0) {
			lastTopic = this.setTags(lastTopic, analysisResults.get(0));
		}
		
		topicList.set(topicList.size()-1, lastTopic);
		
	}
	
	public List<TopicBean> getTopicList(){
		return topicList;
	}
	
	public TopicBean setTags(TopicBean topic, NLUBean nlu) {
		List<String> tags = topic.getTags();
		List<ConceptsResult> concepts = nlu.getConcepts();
		
		if(tags != null && !tags.isEmpty()) {
			for(Object obj : concepts) {
				ConceptsResult concept = (ConceptsResult) obj;
				//resultsと重複していないものをリストに追加
				if(!Arrays.asList(tags).contains(concept.getText())) {
					topic.setTags(this.addTag(concept, tags));
				}
			}
			//tagsが空だった場合新しいリストを作成し、タグを追加
		} else {
			tags = new ArrayList();
			for(Object obj : concepts) {
				ConceptsResult concept = (ConceptsResult) obj;
				topic.setTags(this.addTag(concept, tags));
			}
		}
		
		return topic;
	}
	
	/**
	 * relevanceが0.75以上のconceptをタグとしてタグリストに追加
	 * 
	 * @return List<String> tags 
	 */
	public List<String> addTag(ConceptsResult concept, List<String> tags) {
		if(concept.getRelevance() >= 0.75) {
			tags.add(concept.getText());
		}
		return tags;
	}
}
