package jp.mediahinge.spring.boot.app.connection;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Attributes;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import jp.mediahinge.spring.boot.app.form.ArticleForm;
import jp.mediahinge.spring.boot.app.form.RSSForm;
import jp.mediahinge.spring.boot.app.schedule.ScheduledMethods;
import jp.mediahinge.spring.boot.app.service.ArticleService;

@Component
public class Article {

	Document document;
	
	private ArticleForm articleForm = new ArticleForm();
	private String type = "article";
	private String media;
	private String heading;
	private String first_paragraph;
	private String text = "";
	private String url;
	private String distribution_date;
	private int topics_id = -1;

	public void setArticle(RSSForm rssForm) throws Exception{

		document = Jsoup.connect(rssForm.getUrl()).get();

		//ローカル変数に代入
		media = rssForm.getMedia();
		
		if(media.equals("yomiuri")) {
			processYomiuri();
		}
		else if(media.equals("asahi")) {
			processAsahi();
		}
		else if(media.equals("mainichi")) {
			if(categoryCheck()) {
				processMainichi();
			}
			else {
				return ;
			}
		}

		//articleFormにセット
		Date today = new Date();
		SimpleDateFormat id_format = new SimpleDateFormat("yyyy/MM/dd/");
		if(ScheduledMethods.getArticleCounter() < 10) {
			articleForm.set_id("A" + id_format.format(today) + "00" + ScheduledMethods.getArticleCounter());
		}
		else if(ScheduledMethods.getArticleCounter() < 100) {
			articleForm.set_id("A" + id_format.format(today) + "0" + ScheduledMethods.getArticleCounter());
		}
		else {
			articleForm.set_id("A" + id_format.format(today) + ScheduledMethods.getArticleCounter());
		}
		articleForm.setType(type);
		articleForm.setMedia(media);
		articleForm.setHeading(heading);
		articleForm.setFirst_paragraph(first_paragraph);
		articleForm.setText(text);
		articleForm.setUrl(url);
		articleForm.setDistribution_date(distribution_date);
		articleForm.setTopics_id(topics_id);
	}
	
	public ArticleForm getArticle() {
		
		return articleForm;
	}
	
	public void insertArticle(ArticleService articleService) throws Exception{
		articleService.persist(articleForm);
		Thread.sleep(400);
		
		System.out.println("Successfully inserted article data!");
	}

	public void processYomiuri() {
		System.out.println("processYomiuri");

		Elements elements;
		Attributes attributes;

		//heading
		elements = document.select("h1");
		heading = elements.get(0).text();

		//first_paragraph
		elements = document.getElementsByClass("par1");
		first_paragraph = elements.get(0).text();

		for(Object obj : document.getAllElements()) {
			Element childElement = (Element) obj;
			attributes = childElement.attributes();
			//text
			if(attributes.get("itemprop").equals("articleBody")) {
				text += childElement.text() + "\n";
			}
			//url
			else if(attributes.get("rel").equals("canonical")) {
				url = attributes.get("href");
			}
			//distribution_date
			else if(attributes.get("property").equals("article:published_time")) {
				distribution_date = attributes.get("content");
			}
		}

	}

	public void processAsahi() {
		System.out.println("processAsahi");

		Elements elements;
		Element element;
		Attributes attributes;

		//heading
		elements = document.select("h1");
		heading = elements.get(0).text();

		//first_paragraph
		elements = document.getElementsByClass("ArticleText");
		first_paragraph = elements.get(0).select("p").get(0).text();

		//text
		element = elements.get(0);
		for(Object obj : element.select("p")) {
			Element childElement = (Element) obj;
			text += childElement.text() + "\n";
		}

		for(Object obj : document.getAllElements()) {
			Element childElement = (Element) obj;
			attributes = childElement.attributes();
			//url
			if(attributes.get("rel").equals("canonical")) {
				url = attributes.get("href");
			}
			//distribution_date
			else if(attributes.get("name").equals("pubdate")) {
				distribution_date = attributes.get("content");
			}
		}

	}
	
	public void processMainichi() {

		Elements elements;
		Element element;
		Attributes attributes;

		//heding
		elements = document.select("h1");
		heading = elements.get(0).text();

		element = document.getElementsByClass("main-text").get(0);
		for(Object obj:element.getElementsByClass("txt")) {
			Element childElement = (Element) obj;
			//first_paragraph
			if(first_paragraph == null) {
				first_paragraph = childElement.text();
			}
			//text
			text += childElement.text() + "\n";
		}

		for(Object obj : document.getAllElements()) {
			Element childElement = (Element) obj;
			attributes = childElement.attributes();
			//url
			if(attributes.get("rel").equals("canonical")) {
				url = attributes.get("href");
			}
			//distribution_date
			else if(attributes.get("name").equals("firstcreate")) {
				distribution_date = attributes.get("content");
			}
		}

	}

	public boolean categoryCheck() {
		System.out.println("processMainichi");
		System.out.println("Category check");

		Attributes attributes;

		for(Object obj : document.select("meta")) {
			Element childElement = (Element) obj;
			attributes = childElement.attributes();
			if(attributes.get("name").equals("keywords")) {
				if(attributes.get("content").indexOf("政治") != -1) {
					System.out.println("This is Political News!");
					return true;
				}
			}
		}
		System.out.println("This is not Political News...");
		return false;
	}

}