package jp.mediahinge.spring.boot.app.bean;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ArticleBean extends BaseBean{
//	private String article_id;
	private String media;
	private String heading;
	private String first_paragraph;
	private String text;
	private String url;
	private String distribution_date;
	private int topics_id;
	private double highest_rate;
	
}
