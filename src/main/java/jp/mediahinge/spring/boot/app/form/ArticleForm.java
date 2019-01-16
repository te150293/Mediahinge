package jp.mediahinge.spring.boot.app.form;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ArticleForm extends BaseForm{
//	private String article_id;
	private String media;
	private String heading;
	private String first_paragraph;
	private String text;
	private String url;
	private String distribution_date;
	private String topics_id;
	
}
