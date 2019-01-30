package jp.mediahinge.spring.boot.app.form;

import lombok.Data;

@Data
public class ArticleForm {
	private String _id;
	private String media;
	private String heading;
	private String first_paragraph;
	private String url;
}
