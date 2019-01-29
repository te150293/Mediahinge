package jp.mediahinge.spring.boot.app.form;

import lombok.Data;
/**
 * 
 * 更新候補のTopicとScoreを格納するクラス
 * 
 * @author 150293
 * 
 */
@Data
public class HighestScoreTopic {
	/**
	 * 対象のTopic
	 */
	private TopicForm topicForm;
	
	/**
	 * 更新したいScore
	 */
	private Score score;
}
