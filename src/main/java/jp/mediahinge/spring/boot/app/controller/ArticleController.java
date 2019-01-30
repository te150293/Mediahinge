package jp.mediahinge.spring.boot.app.controller;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import jp.mediahinge.spring.boot.app.bean.ArticleBean;
import jp.mediahinge.spring.boot.app.service.ArticleService;

@Controller
@RequestMapping("articles")
public class ArticleController {

	@Autowired
	ArticleService service;

	/**
	 * ModelにFormを初期セットする
	 * 
	 * @return ArticleForm
	 */
	@ModelAttribute 
	ArticleBean setUpForm() {
		return new ArticleBean();
	}

	/**
	 * 見出し表示
	 * 
	 * @param model
	 * @return
	 */
	@GetMapping
	public String list(Model model) {
		System.out.println("debug:ArticleController:01");
		if(service == null) {
			System.out.println("debug:service is null");
			System.out.println("debug:ArticleController:02");
			return "articles/persist";
		}

		List<ArticleBean> articlesList = new ArrayList<ArticleBean>();
		for (ArticleBean doc : service.getAll()) {
			String _id = doc.get_id();
			if (_id != null){
				ArticleBean form = new ArticleBean();
				form.set_id(_id);;
				articlesList.add(form);
			}
		}
		model.addAttribute("articles", articlesList);
		System.out.println("debug:ArticleController:03");
		return "articles/persist";
	}

	@PostMapping
	public String test(Model model) {
		Date today = new Date();
		SimpleDateFormat id_format = new SimpleDateFormat("yyyyMMddHHmmss");
		ArticleBean articleForm = new ArticleBean();
		articleForm.setMedia("test");
		System.out.println("debug:insert test data");
		service.persist(articleForm);
		System.out.println("debug:successfully insert test data");
		service.shutDown();
		System.out.println("debug:successfully shutdown the connection");
		return "redirect:/articles";
	}
}

