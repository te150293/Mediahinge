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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
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
public class TopicController {

	@Autowired
	private ArticleService articleService;
	@Autowired
	private NLUService nluService;
	@Autowired
	private TopicService topicService;
	/**
	 * モデルを初期化する
	 * 
	 * @return TopicsForm モデルにセットするTopicsForm
	 */
	@ModelAttribute
	TopicBean setUpForm() {
		return new TopicBean();
	}

	@RequestMapping(method = RequestMethod.GET, value="/")
	public String top(Model model) {
		List<TopicBean> recentTopicsBean = topicService.getSortedTopic(0);
		System.out.println(recentTopicsBean + "\n");
		
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
		model.addAttribute("page", 1);
		model.addAttribute("number",topicFormList.size());
		
		return "users/user_top";
	}
	
	@RequestMapping(method = RequestMethod.GET, value="/", params = {"page"})
	public String top(Model model, @RequestParam("page") int page) throws InterruptedException {
		int skip;
		if(page == 0) {
			skip = 1;
		}else {
			skip = page;
		}
		skip = (skip -1)* 10;
		List<TopicBean> recentTopicsBean = topicService.getSortedTopic(skip);
		System.out.println(recentTopicsBean + "\n");
		
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
					Thread.sleep(200);
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
		model.addAttribute("page", page);
		model.addAttribute("number",topicFormList.size());
		
		return "users/user_top";
	}
	
	@GetMapping(path = "search")
	public String search(Model model, @RequestParam("tag") String tag , @RequestParam("page") int page) throws InterruptedException {
		String str = tag;
		if(str != null) {
			str = str.replace("&", "&amp;");
			str = str.replace("\"", "&quot;");
			str = str.replace("<", "&lt;");
			str = str.replace(">", "&gt;");
			str = str.replace("'", "&#39;");
		}
		int skip = (page -1)* 10;
		List<TopicBean> resultTopicsBean = topicService.searchByTag(str,skip);

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
					Thread.sleep(200);
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
		model.addAttribute("page", page);
		model.addAttribute("number",topicFormList.size());

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
		List<ArticleBean> articleBeans = articleService.runQuery(selector);
		List<String> article_idList = new ArrayList<>();
		if(articleBeans != null) {
			for(Object obj : articleBeans) {
				ArticleBean articleBean = (ArticleBean)obj;
				article_idList.add(articleBean.get_id());
			}
		}
		topicService.searchTopicByArticle_id(article_idList);

		return "users/user_top";
	}

}
