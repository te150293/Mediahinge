package jp.mediahinge.spring.boot.app.bean;

import java.util.List;

import com.ibm.watson.developer_cloud.natural_language_understanding.v1.model.EntitiesResult;

import lombok.Data;

@Data
public class TopicBean extends BaseBean{
	/**
	 * topic id
	 * 
	 * format:yyyyMMddxx
	 */
	private int topic_id;
	private List<Score> article_list;
	private List<String> tags;
//	private List<EntitiesResult> entities;
}
