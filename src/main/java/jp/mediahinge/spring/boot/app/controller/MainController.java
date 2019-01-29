package jp.mediahinge.spring.boot.app.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestParam;

import jp.mediahinge.spring.boot.app.form.ArticleForm;
import jp.mediahinge.spring.boot.app.form.TopicForm;
import jp.mediahinge.spring.boot.app.processing.Topics;
import jp.mediahinge.spring.boot.app.service.ArticleService;
import jp.mediahinge.spring.boot.app.service.NLUService;

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
	 * モデルを初期化する
	 * 
	 * @return TopicsForm モデルにセットするTopicsForm
	 */
	@ModelAttribute
	TopicForm setUpForm() {
		return new TopicForm();
	}
	
	@GetMapping(path = "top")
	public String top(Model model) {
		
		List<ArticleForm> sortedResults = articleService.getArticlesSortedByTopics_id();
		Topics topics = new Topics();
		topics.grouping(sortedResults,nluService);
		
		List<TopicForm> topicList = topics.getTopicList();

		System.out.println("topicList:" + topicList);
//		for(Object obj : topicList) {
//			System.out.println(obj);
//		}
		model.addAttribute("topics",topicList);
		
		return "users/user_top";
	}
	

	@GetMapping(path = "search")
	public String search(Model model, @RequestParam("tag") String tag) {

		return "users/user_top";
	}

}
