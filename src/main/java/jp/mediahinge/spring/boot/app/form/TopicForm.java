package jp.mediahinge.spring.boot.app.form;

import java.util.List;

import lombok.Data;

@Data
public class TopicForm {
	private int topic_id;
	private List<ArticleForm> articles;
	private List<String> tags;
}
