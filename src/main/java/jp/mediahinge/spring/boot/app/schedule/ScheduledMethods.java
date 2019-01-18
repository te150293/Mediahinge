package jp.mediahinge.spring.boot.app.schedule;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import jp.mediahinge.spring.boot.app.connection.Article;
import jp.mediahinge.spring.boot.app.connection.NLU;
import jp.mediahinge.spring.boot.app.connection.RSS;
import jp.mediahinge.spring.boot.app.form.ArticleForm;
import jp.mediahinge.spring.boot.app.form.NLUForm;
import jp.mediahinge.spring.boot.app.form.RSSForm;
import jp.mediahinge.spring.boot.app.service.CloudantArticleService;
import jp.mediahinge.spring.boot.app.service.CloudantNLUService;
import jp.mediahinge.spring.boot.app.service.CloudantRSSService;
@Component
public class ScheduledMethods {

	@Autowired
	private CloudantNLUService nluService;

	@Autowired
	private CloudantRSSService rssService;
	
	@Autowired
	private CloudantArticleService articleService;
	
	private static int count = 1;

	@Scheduled(cron = "0 0 * * * *")
//	@Scheduled(cron = "0 * * * * *")
//	@Scheduled(cron = "10 * * * * *")
//	@Scheduled(cron = "20 * * * * *")
//	@Scheduled(cron = "30 * * * * *")
//	@Scheduled(cron = "40 * * * * *")
//	@Scheduled(cron = "50 * * * * *")
	public void insertData() throws Exception{
		
		RSS rss = new RSS();

		rss.setRSSList(rssService);
		rss.insertRSS(rssService);
		List<RSSForm> insertedRSSList = rss.getRSSList();

		for(Object obj :insertedRSSList) {
			RSSForm rssForm = (RSSForm)obj;
			
			Article article = new Article();
			article.setArticle(rssForm);

			ArticleForm temp_ArticleForm = article.getArticle();
			if(temp_ArticleForm.getType()!= null) {
				article.insertArticle(articleService);
				if(temp_ArticleForm.getText() != null) {
					NLU nlu = new NLU();
					nlu.setNLUForm(temp_ArticleForm);
					nlu.insertAnalysisResults(nluService);
					System.out.println();
				}
			}
		}
	}
	
	@Scheduled(cron = "0 0 0 * * *")
	public void resetCount() throws Exception{
		count = 1;
	}
	
	public static int getCount() {
		return count;
	}
	
	public static void incrementCount() {
		count++;
	}
}
