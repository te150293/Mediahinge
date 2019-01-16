package jp.mediahinge.spring.boot.app.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import jp.mediahinge.spring.boot.app.connection.NLU;

@Controller
@RequestMapping("nlu")
public class NLUClontroller {

	@Autowired
	NLU service;
	
	@GetMapping
	public String mapping_of_test() {
		return "nlu/test";
	}

}
