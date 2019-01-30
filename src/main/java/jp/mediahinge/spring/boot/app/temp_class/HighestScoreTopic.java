package jp.mediahinge.spring.boot.app.temp_class;

import jp.mediahinge.spring.boot.app.bean.Score;
import jp.mediahinge.spring.boot.app.bean.TopicBean;
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
	private TopicBean topicForm;
	
	/**
	 * 更新したいScore
	 */
	private Score score;
}
