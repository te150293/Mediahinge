package jp.mediahinge.spring.boot.app.form;

import java.util.List;

import lombok.Data;

@Data
public class TopicForm extends BaseForm{
	/**
	 * topic id
	 * 
	 * format:yyyyMMddxxx
	 */
	private int topic_id;
	private List<Score> article_list;
	private List<String> tags;
}
