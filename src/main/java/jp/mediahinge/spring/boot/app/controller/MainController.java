package jp.mediahinge.spring.boot.app.controller;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import jp.mediahinge.spring.boot.app.bean.ArticleBean;
import jp.mediahinge.spring.boot.app.bean.Score;
import jp.mediahinge.spring.boot.app.bean.TopicBean;
import jp.mediahinge.spring.boot.app.form.ArticleForm;
import jp.mediahinge.spring.boot.app.form.TopicForm;
import jp.mediahinge.spring.boot.app.processing.Topics;
import jp.mediahinge.spring.boot.app.service.ArticleService;
import jp.mediahinge.spring.boot.app.service.NLUService;
import jp.mediahinge.spring.boot.app.service.Selector;
import jp.mediahinge.spring.boot.app.service.TopicService;

@Controller
public class MainController {

	/**
	 * Article用サービス
	 */
	@Autowired
	ArticleService articleService;

	/**
	 * NLU用サービス
	 */
	@Autowired
	NLUService nluService;

	/**
	 * Topic用サービス
	 */
	@Autowired
	TopicService topicService;
	/**
	 * モデルを初期化する
	 * 
	 * @return TopicsForm モデルにセットするTopicsForm
	 */
	@ModelAttribute
	TopicBean setUpForm() {
		return new TopicBean();
	}
	
	@GetMapping(path = "top")
	public String top(Model model) {

		Calendar calendar = Calendar.getInstance();
		Date today = new Date();
		calendar.setTime(today);
		calendar.add(Calendar.DATE, -2);
		Date yes_yesterday = calendar.getTime();
		SimpleDateFormat id_format = new SimpleDateFormat("yyyyMMdd");
		String yes_yesterday_topic = id_format.format(yes_yesterday) + "00";
		
		List<TopicBean> recentTopicsBean = topicService.getRecentTopics(Integer.parseInt(yes_yesterday_topic));
		
		List<TopicForm> topicFormList = new ArrayList<>();
		for(Object obj1 : recentTopicsBean) {
			TopicBean topicBean = (TopicBean) obj1;
			if(topicBean.getArticle_list().size() > 1) {
				TopicForm topicForm = new TopicForm();
				topicForm.setTopic_id(topicBean.getTopic_id());
				topicForm.setTags(topicBean.getTags());
				
				List<ArticleForm> articleFormList = new ArrayList<>();
				for(Object obj2 : topicBean.getArticle_list()) {
					Score score = (Score)obj2;
					ArticleBean articleBean = articleService.get(score.getArticle_id());
					ArticleForm articleForm = new ArticleForm();
					articleForm.set_id(articleBean.get_id());
					articleForm.setMedia(articleBean.getMedia());
					articleForm.setFirst_paragraph(articleBean.getFirst_paragraph());
					articleForm.setHeading(articleBean.getHeading());
					articleForm.setUrl(articleBean.getUrl());
					
					articleFormList.add(articleForm);
				}
				topicForm.setArticles(articleFormList);
				topicFormList.add(topicForm);
			}
		}
//		List<TopicForm> topicList = topics.getTopicList();

//		System.out.println("topicList:" + topicList);
//		for(Object obj : topicList) {
//			System.out.println(obj);
//		}
		model.addAttribute("topics",topicFormList);
		model.addAttribute("tag", null);
		
		return "users/user_top";
	}
	

	@GetMapping(path = "search")
	public String search(Model model, @RequestParam("tag") String tag) {

		String str = tag;
		
		if(str != null) {
			str = str.replace("&", "&amp;");
			str = str.replace("\"", "&quot;");
			str = str.replace("<", "&lt;");
			str = str.replace(">", "&gt;");
			str = str.replace("'", "&#39;");
		}
		
		List<TopicBean> resultTopicsBean = topicService.searchByTag(str);

		List<TopicForm> topicFormList = new ArrayList<>();
		for(Object obj1 : resultTopicsBean) {
			TopicBean topicBean = (TopicBean) obj1;
			if(topicBean.getArticle_list().size() > 1) {
				TopicForm topicForm = new TopicForm();
				topicForm.setTopic_id(topicBean.getTopic_id());
				topicForm.setTags(topicBean.getTags());
				
				List<ArticleForm> articleFormList = new ArrayList<>();
				for(Object obj2 : topicBean.getArticle_list()) {
					Score score = (Score)obj2;
					ArticleBean articleBean = articleService.get(score.getArticle_id());
					ArticleForm articleForm = new ArticleForm();
					articleForm.set_id(articleBean.get_id());
					articleForm.setMedia(articleBean.getMedia());
					articleForm.setFirst_paragraph(articleBean.getFirst_paragraph());
					articleForm.setHeading(articleBean.getHeading());
					articleForm.setUrl(articleBean.getUrl());
					
					articleFormList.add(articleForm);
				}
				topicForm.setArticles(articleFormList);
				topicFormList.add(topicForm);
			}
		}
		model.addAttribute("topics",topicFormList);
		model.addAttribute("tag", tag);

		return "users/user_top";
	}
	
	@PostMapping(path = "search")
	public String searchText(Model model, @RequestParam("search") String search) {
		String text = null;
		if(search != null) {
			text = search;
			
			text = text.replace("&", "&amp;");
			text = text.replace("\"", "&quot;");
			text = text.replace("<", "&lt;");
			text = text.replace(">", "&gt;");
			text = text.replace("'", "&#39;");
		}

		String selector = new Selector().buildTextSelector(text);
		List<ArticleBean> articles = articleService.runQuery(selector);
		if(articles != null) {
			for(Object obj : articles) {
				ArticleBean articleBean = (ArticleBean)obj;
				
			}
		}
		
		return "users/user_top";
	}

}
