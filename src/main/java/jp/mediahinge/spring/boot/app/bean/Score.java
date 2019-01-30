package jp.mediahinge.spring.boot.app.bean;

import lombok.Data;
/**
 * 
 * Topicに格納するarticle_idとmedia
 * scoreは対象Topicとの一致率
 * 
 * @author 150293
 *
 */
@Data
public class Score {
	private String article_id;
	private String media;
	private double score;
}
