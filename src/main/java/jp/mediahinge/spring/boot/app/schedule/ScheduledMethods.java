package jp.mediahinge.spring.boot.app.schedule;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.google.gson.Gson;
import com.ibm.watson.developer_cloud.natural_language_understanding.v1.model.ConceptsResult;
import com.ibm.watson.developer_cloud.natural_language_understanding.v1.model.EntitiesResult;

import jp.mediahinge.spring.boot.app.bean.ArticleBean;
import jp.mediahinge.spring.boot.app.bean.NLUBean;
import jp.mediahinge.spring.boot.app.bean.RSSBean;
import jp.mediahinge.spring.boot.app.bean.Score;
import jp.mediahinge.spring.boot.app.bean.TopicBean;
import jp.mediahinge.spring.boot.app.connection.Analyzer;
import jp.mediahinge.spring.boot.app.connection.Article;
import jp.mediahinge.spring.boot.app.connection.NLU;
import jp.mediahinge.spring.boot.app.connection.RSS;
import jp.mediahinge.spring.boot.app.connection.Topic;
import jp.mediahinge.spring.boot.app.service.ArticleService;
import jp.mediahinge.spring.boot.app.service.NLUService;
import jp.mediahinge.spring.boot.app.service.RSSService;
import jp.mediahinge.spring.boot.app.service.TopicService;
import jp.mediahinge.spring.boot.app.temp_class.HighestScoreTopic;
import jp.mediahinge.spring.boot.app.temp_class.RateForm;


@Component
public class ScheduledMethods {

	@Autowired
	private NLUService nluService;

	@Autowired
	private RSSService rssService;

	@Autowired
	private ArticleService articleService;

	@Autowired
	private TopicService topicService;

	private List<NLUBean> nluFormList = new ArrayList<>();

	private static int articleCounter = 76;
	private static int rssCounter = 95;

	private static int topic_id = 52;

//		@Scheduled(cron = "0 0 * * * *")
//		@Scheduled(cron = "0 * * * * *")
//		@Scheduled(cron = "20 * * * * *")
//		@Scheduled(cron = "40 * * * * *")
	public void insertData() throws Exception{

		RSS rss = new RSS();

		rss.setRSSList(rssService);
		rss.insertRSS(rssService);
		List<RSSBean> insertedRSSList = rss.getRSSList();

		for(Object obj1 :insertedRSSList) {
			RSSBean rssForm = (RSSBean) obj1;

			Article article = new Article();
			article.setArticle(rssForm);

			ArticleBean tempArticleForm = article.getArticle();
			if(tempArticleForm.getType()!= null && tempArticleForm.getText() != null) {
				article.insertArticle(articleService);
				this.incrementArticleCounter();

				//NLUとPythonに解析リクエスト
				//Pythonに新着URLを送信
				String urlString = "https://mhanalysispython.mybluemix.net/";
				HttpURLConnection httpURLConnection = null;
				NLUBean resultsFromPython = null;
				
				//Python接続処理

				NLU nlu = new NLU();
				nlu.setNLUForm(tempArticleForm,resultsFromPython);
				//Pythonに渡すNLUForm(解析結果)のリスト
				nluFormList.add(nlu.getNLUForm());
				nlu.insertAnalysisResults(nluService);
				System.out.println();

				//temp_ArticleFormのConceptsを取得
				List<ConceptsResult> tempConcepts = nlu.getConceptsResults();
				List<EntitiesResult> tempEntities = nlu.getEntitiesResult();
				Topic topic = new Topic();
				//過去二日間分のTopicを取得
				List<TopicBean> topicList = topic.getPastTopics(topicService);
				if(topicList == null) {
					//Topic新規作成処理
					topic.createTopic(topicService, tempArticleForm, tempConcepts, tempEntities);
					Thread.sleep(400);

				} else {
					HighestScoreTopic highestScoreTopic = new HighestScoreTopic();
					
					Score highestScore = new Score();
					highestScore.setArticle_id(tempArticleForm.get_id());
					highestScore.setMedia(tempArticleForm.getMedia());
					highestScore.setScore(0);

					//temp_conceptsとtopicのTagsからScoreを算出
					for(Object obj2 : topicList) {
						TopicBean tempTopic = (TopicBean) obj2;
						
						double tempScore = 0;
						
						//topicFormのTagsとconceptsを比較、一致していた場合relevanceをtempScoreに加算
						List<String> tags = tempTopic.getTags();
						for(Object obj3 : tags) {
							String tag = (String) obj3;
							for(Object obj4 : tempConcepts) {
								ConceptsResult concept = (ConceptsResult) obj4;
								if(tag.equals(concept.getText())){
									tempScore += concept.getRelevance();
								}
							}
						}
						
						if(highestScore.getScore() < tempScore) {
							highestScoreTopic.setTopicForm(tempTopic);
							
							highestScore.setScore(tempScore);
							highestScoreTopic.setScore(highestScore);
						}
					}
					
					if(highestScore.getScore() < 1.2) {
						//tempArticleをグループ化できるトピック無しとみなす
						//Topic新規作成処理
						topic.createTopic(topicService, tempArticleForm, tempConcepts, tempEntities);
						Thread.sleep(400);
					} else {
						topic.updateTopic(topicService, highestScoreTopic, tempConcepts, tempEntities);
						Thread.sleep(400);
					}
					
				}
			}
		}
	}

	@Scheduled(cron = "0 0 0 * * *")
	public void resetCounter(){
		articleCounter = 1;
		rssCounter = 1;
		topic_id = 1;
	}

	public static int getArticleCounter() {
		return articleCounter;
	}

	public void incrementArticleCounter() {
		articleCounter = articleCounter + 1;
	}

	public static int getRSSCounter() {
		return rssCounter;
	}

	public static void incrementRSSCounter() {
		rssCounter = rssCounter + 1;
	}

	public static int getTopic_id() {
		return topic_id;
	}

	public static void incrementTopic_id() {
		topic_id = topic_id + 1;
	}
	//	@Scheduled(cron = "0 * * * * *")
	//	@Scheduled(cron = "10 * * * * *")
	//	@Scheduled(cron = "20 * * * * *")
	//	@Scheduled(cron = "30 * * * * *")
	//	@Scheduled(cron = "40 * * * * *")
	//	@Scheduled(cron = "50 * * * * *")
//	@Scheduled(initialDelay = 6000, fixedRate = 500000)
	public void test() throws Exception{

		Calendar calendar = Calendar.getInstance();
		Date today = new Date();
		calendar.setTime(today);
		calendar.add(Calendar.DATE, -2);
		Date yes_yesterday = calendar.getTime();
		SimpleDateFormat id_format = new SimpleDateFormat("yyyyMMdd");
		String yes_yesterday_topic = id_format.format(yes_yesterday) + "00";
		
		System.out.println(Integer.parseInt(yes_yesterday_topic));
//
//		String urlString = "https://mhanalysispython.mybluemix.net/";
//		HttpURLConnection httpURLConnection = null;
//
//		List<RSSForm> rssList = new ArrayList<>();
//		RSSForm testRSS = new RSSForm();
//		testRSS.setUrl("https://www.yomiuri.co.jp/politics/20190124-OYT1T50118.html");
//		rssList.add(testRSS);
//		testRSS = new RSSForm();
//		testRSS.setUrl("https://www.asahi.com/articles/ASM1T3PGTM1TUTFK008.html");
//		rssList.add(testRSS);
//		testRSS = new RSSForm();
//		testRSS.setUrl("https://mainichi.jp/articles/20190125/k00/00m/010/114000c");
//		rssList.add(testRSS);
//
//		NLUForm resultsFromPython = null;
//		try {
//			for(int i = 0; i < 3; i++) {
//				httpURLConnection = Analyzer.connectAnalyzer(httpURLConnection, urlString);
//
//				//コネクションを開く
//				httpURLConnection.connect();
//
//				RSSForm rss = rssList.get(i);
//
//				//POSTするJSONオブジェクト
//				JSONObject json = new JSONObject();
//				json.put("url",rss.getUrl());
//
//				//リクエストボディの書き出しを行う
//				OutputStream outputStream = httpURLConnection.getOutputStream();//OutputStreamを取得
//				Analyzer.postJson(outputStream, json);
//				Thread.sleep(5000);
//
//				//ResponseBody の読み出しを行う
//				StringBuffer stringBuffer = Analyzer.getResponceBody(httpURLConnection);
//
//				Gson gson = new Gson();
//				resultsFromPython = gson.fromJson(stringBuffer.toString(), NLUForm.class);
//
//				System.out.println(resultsFromPython + "\n");
//
//				httpURLConnection.disconnect();
//				httpURLConnection = null;
//
//				httpURLConnection = Analyzer.connectAnalyzer(httpURLConnection, urlString);
//
//				//コネクションを開く
//				httpURLConnection.connect();
//
//				//POSTするJSONオブジェクト
//				json = new JSONObject();
//				json.put("_id","A2000/01/01/00" + i);
//				json.put("distribution_date","2000/01/01");
//				if(i == 0) {
//					json.put("media","yomiuri");
//				} else if(i == 1){
//					json.put("media","asahi");
//				} else {
//					json.put("media","mainichi");
//				}
//				json.put("results",resultsFromPython.getResults());
//
//				//リクエストボディの書き出しを行う
//				outputStream = null;
//				outputStream = httpURLConnection.getOutputStream();//OutputStreamを取得
//				System.out.println(json + "\n");
//				Analyzer.postJson(outputStream, json);
//				Thread.sleep(5000);
//
//				System.out.println(httpURLConnection.getResponseCode() + "\n");
//
//				//ResponseBody の読み出しを行う
//				stringBuffer = Analyzer.getResponceBody(httpURLConnection);
//				System.out.println(stringBuffer.toString() + "\n");
//
//				resultsFromPython = null;
//				httpURLConnection.disconnect();
//				httpURLConnection = null;
//			}
//			httpURLConnection = Analyzer.connectAnalyzer(httpURLConnection, urlString);
//
//			//コネクションを開く
//			httpURLConnection.connect();
//
//			//POSTするJSONオブジェクト
//			JSONObject json = new JSONObject();
//			json.put("_id","A2000/01/01/000");
//
//			//リクエストボディの書き出しを行う
//			OutputStream outputStream = null;
//			outputStream = httpURLConnection.getOutputStream();//OutputStreamを取得
//			Analyzer.postJson(outputStream, json);
//			Thread.sleep(5000);
//
//			System.out.println(httpURLConnection.getResponseCode() + "\n");
//
//			//			ResponseBody の読み出しを行う
//			StringBuffer stringBuffer = Analyzer.getResponceBody(httpURLConnection);
//			System.out.println(stringBuffer.toString() + "\n");
//			Gson gson = new Gson();
//			RateForm rateForm = gson.fromJson(stringBuffer.toString(), RateForm.class);
//			System.out.println(rateForm + "\n");
//
//			resultsFromPython = null;
//			httpURLConnection.disconnect();
//
//		} catch (MalformedURLException e) {
//			System.err.println("Invalid URL format: " + urlString);
//		} catch (Exception e) {
//			e.printStackTrace();
//		} finally {
//			if (httpURLConnection != null) {
//				//コネクションを閉じる。
//				httpURLConnection.disconnect();
//			}
//		}
	}

//	@Scheduled(cron = "0 20 * * * *")
	public void registrationRequest() throws Exception{
		for(Object obj : nluFormList) {
			NLUBean nluForm = (NLUBean) obj;

			String urlString = "https://mhanalysispython.mybluemix.net/";
			HttpURLConnection httpURLConnection = null;
			try {
				httpURLConnection = Analyzer.connectAnalyzer(httpURLConnection, urlString);

				//コネクションを開く
				httpURLConnection.connect();

				//リクエストボディの書き出しを行う
				OutputStream outputStream = httpURLConnection.getOutputStream();//OutputStreamを取得

				String id = nluForm.getArticle_id();
				String distribution_date = nluForm.getArticle_id().substring(1, 11);
				List<String> results = nluForm.getResults();

				JSONObject json = new JSONObject();//POSTするJSONオブジェクト
				json.put("_id",id);
				json.put("distribution_date",distribution_date);
				json.put("results",results);

				Analyzer.postJson(outputStream, json);
				Thread.sleep(5000);

			} catch (MalformedURLException e) {
				System.err.println("Invalid URL format: " + urlString);
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				if (httpURLConnection != null) {
					//コネクションを閉じる。
					httpURLConnection.disconnect();
				}
			}
		}
		nluFormList = new ArrayList<>();
	}

//	@Scheduled(cron = "0 30 * * * *")
	public void requireRate() throws Exception{
		List<ArticleBean> notGroupedArticleList = articleService.getNotGroupedArticle();

		String urlString = "https://mhanalysispython.mybluemix.net/";
		HttpURLConnection httpURLConnection = null;
		RateForm rateForm = null;//受信したURL格納用
		for(Object obj : notGroupedArticleList) {
			ArticleBean articleForm0 = (ArticleBean) obj;

			try {
				//Analyzer への connection の設定を行う
				httpURLConnection = Analyzer.connectAnalyzer(httpURLConnection, urlString);

				//コネクションを開く
				httpURLConnection.connect();

				//リクエストボディの書き出しを行う
				OutputStream outputStream = httpURLConnection.getOutputStream();//OutputStreamを取得

				String id = articleForm0.get_id();

				JSONObject json = new JSONObject();//POSTするJSONオブジェクト
				json.put("_id",id);

				Analyzer.postJson(outputStream, json);
				Thread.sleep(5000);

				//ResponseBody の読み出しを行う
				StringBuffer stringBuffer = Analyzer.getResponceBody(httpURLConnection);

				System.out.println(stringBuffer.toString());
				Gson gson = new Gson();

				rateForm = gson.fromJson(stringBuffer.toString(), RateForm.class);

			} catch (MalformedURLException e) {
				System.err.println("Invalid URL format: " + urlString);
			} catch (Exception e) {
				e.printStackTrace();
			}

			if (httpURLConnection != null) {
				//コネクションを閉じる。
				httpURLConnection.disconnect();
			}

			if(rateForm != null) {

				//一致率によってグループ化するか判定する処理を記述する

				ArticleBean articleForm1 = articleService.get(rateForm.get_id1());
				Thread.sleep(400);
				if(articleForm1.getHighest_rate() <= rateForm.getRate1()) {
					articleForm0.setHighest_rate(rateForm.getRate1());
					articleForm1.setHighest_rate(rateForm.getRate1());
					articleForm0.setTopics_id(topic_id);
					articleForm1.setTopics_id(topic_id);
				}
				ArticleBean articleForm2 = articleService.get(rateForm.get_id2());
				Thread.sleep(400);
				if(articleForm2.getHighest_rate() <= rateForm.getRate2()) {
					articleForm0.setHighest_rate(rateForm.getRate2());
					articleForm2.setHighest_rate(rateForm.getRate2());
					articleForm0.setTopics_id(topic_id);
					articleForm2.setTopics_id(topic_id);
				}

				articleService.updateTopics_id(articleForm0.get_id(), articleForm0);
				Thread.sleep(400);
				articleService.updateTopics_id(articleForm1.get_id(), articleForm1);
				Thread.sleep(400);
				articleService.updateTopics_id(articleForm1.get_id(), articleForm1);
				Thread.sleep(400);
			}
		}
	}
}


