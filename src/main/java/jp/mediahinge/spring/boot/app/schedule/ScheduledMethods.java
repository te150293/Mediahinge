package jp.mediahinge.spring.boot.app.schedule;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.google.gson.Gson;

import jp.mediahinge.spring.boot.app.connection.Article;
import jp.mediahinge.spring.boot.app.connection.NLU;
import jp.mediahinge.spring.boot.app.connection.RSS;
import jp.mediahinge.spring.boot.app.form.ArticleForm;
import jp.mediahinge.spring.boot.app.form.NLUForm;
import jp.mediahinge.spring.boot.app.form.RSSForm;
import jp.mediahinge.spring.boot.app.form.RateForm;
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

	private List<NLUForm> nluFormList = new ArrayList<>();
	
	private static int articleCounter = 75;
	private static int rssCounter = 95;
	
	private static int topics_id = 1;

	@Scheduled(cron = "0 0 * * * *")
//	@Scheduled(cron = "0 * * * * *")
//	@Scheduled(cron = "20 * * * * *")
//	@Scheduled(cron = "40 * * * * *")
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
				this.incrementArticleCounter();
				
				//NLUとPythonに解析リクエスト
				if(temp_ArticleForm.getText() != null) {
					
					//Pythonに新着URLを送信
					String urlString = "Pythonの形態素解析URL";
					HttpURLConnection httpURLConnection = null;
					NLUForm resultsFromPython = null;
					try {
						//接続URLを決める。
						URL url = new URL(urlString);
						//URLへのコネクションを取得する。
						httpURLConnection = (HttpURLConnection) url.openConnection();

						httpURLConnection.setConnectTimeout(100000);//接続タイムアウトを設定する。
						httpURLConnection.setReadTimeout(100000);//レスポンスデータ読み取りタイムアウトを設定する。
						httpURLConnection.setRequestProperty("User-Agent", "Mediahinge");// ヘッダを設定
						httpURLConnection.setRequestProperty("Accept-Language", "ja");// ヘッダを設定
						httpURLConnection.addRequestProperty("Content-Type", "application/json; charset=UTF-8");
						httpURLConnection.setRequestMethod("POST");//HTTPのメソッドをPOSTに設定する。
						httpURLConnection.setDoOutput(true);//リクエストのボディ送信を許可する
						httpURLConnection.setDoInput(true);//レスポンスのボディ受信を許可する
						
						//コネクションを開く
						httpURLConnection.connect();
						
						//リクエストボディの書き出しを行う
						OutputStream outputStream = httpURLConnection.getOutputStream();//OutputStreamを取得

						JSONObject json = new JSONObject();//POSTするJSONオブジェクト
						json.put("url",rssForm.getUrl());
						
						PrintStream printStream = new PrintStream(outputStream);
						printStream.print(json.toString());//データをPOSTする
						printStream.close();
						
						outputStream.close();

						//レスポンスボディの読み出しを行う。
						InputStream inputStream = httpURLConnection.getInputStream();
						BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
						StringBuffer stringBuffer = new StringBuffer();
						String line = "";
						while ((line = bufferedReader.readLine()) != null) {
							stringBuffer.append(line);
						}
						bufferedReader.close();
						inputStream.close();

						System.out.println(stringBuffer.toString());
						Gson gson = new Gson();
						resultsFromPython = gson.fromJson(stringBuffer.toString(), NLUForm.class);

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
					
					NLU nlu = new NLU();
					nlu.setNLUForm(temp_ArticleForm,resultsFromPython);
					nluFormList.add(nlu.getNLUForm());
					nlu.insertAnalysisResults(nluService);
					System.out.println();
				}
			}
		}
	}

	@Scheduled(cron = "0 0 0 * * *")
	public void resetCounter(){
		articleCounter = 1;
		rssCounter = 1;
	}

	public static int getArticleCounter() {
		return articleCounter;
	}

	public static void incrementArticleCounter() {
		articleCounter = articleCounter + 1;
	}

	public static int getRSSCounter() {
		return rssCounter;
	}

	public static void incrementRSSCounter() {
		rssCounter = rssCounter + 1;
	}
	
//	@Scheduled(cron = "0 * * * * *")
//	@Scheduled(cron = "10 * * * * *")
//	@Scheduled(cron = "20 * * * * *")
//	@Scheduled(cron = "30 * * * * *")
//	@Scheduled(cron = "40 * * * * *")
//	@Scheduled(cron = "50 * * * * *")
	public void test() throws Exception{
		System.out.println("test");
		ArticleForm newArticleForm = new ArticleForm();
		newArticleForm.setTopics_id(445566);
		articleService.updateTopics_id("2019/01/21/4", newArticleForm);
	}

//	@Scheduled(cron = "0 20 * * * *")
	public void registrationRequest() throws Exception{
		for(Object object : nluFormList) {
			NLUForm nluForm = (NLUForm) object;
			
			String urlString = "Pythonの解析結果登録URL";
			HttpURLConnection httpURLConnection = null;
			try {
				//接続URLを決める。
				URL url = new URL(urlString);
				//URLへのコネクションを取得する。
				httpURLConnection = (HttpURLConnection) url.openConnection();

				httpURLConnection.setConnectTimeout(100000);//接続タイムアウトを設定する。
				httpURLConnection.setReadTimeout(100000);//レスポンスデータ読み取りタイムアウトを設定する。
				httpURLConnection.setRequestProperty("User-Agent", "Mediahinge");// ヘッダを設定
				httpURLConnection.setRequestProperty("Accept-Language", "ja");// ヘッダを設定
				httpURLConnection.addRequestProperty("Content-Type", "application/json; charset=UTF-8");
				httpURLConnection.setRequestMethod("POST");//HTTPのメソッドをPOSTに設定する。
				httpURLConnection.setDoOutput(true);//リクエストのボディ送信を許可する
				httpURLConnection.setDoInput(true);//レスポンスのボディ受信を許可する
				
				//コネクションを開く
				httpURLConnection.connect();
				
				//リクエストボディの書き出しを行う
				OutputStream outputStream = httpURLConnection.getOutputStream();//OutputStreamを取得

				String id = nluForm.getArticle_id();
				String distribution_date = nluForm.getArticle_id().substring(0, 10);
				List<String> results = nluForm.getResults();
				
				JSONObject json = new JSONObject();//POSTするJSONオブジェクト
				json.put("_id",id);
				json.put("distribution_date",distribution_date);
				json.put("results",results);
				
				PrintStream printStream = new PrintStream(outputStream);
				printStream.print(json.toString());//データをPOSTする
				printStream.close();
				
				outputStream.close();

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
		List<ArticleForm> notGroupedArticleList = articleService.getNotGroupedArticle();
		
		String urlString = "Pythonの一致率返却URL";
		HttpURLConnection httpURLConnection = null;
		RateForm rateForm = null;//受信したURL格納用
		for(Object object : notGroupedArticleList) {
			ArticleForm articleForm0 = (ArticleForm) object;
			
			try {
				//接続URLを決める。
				URL url = new URL(urlString);
				//URLへのコネクションを取得する。
				httpURLConnection = (HttpURLConnection) url.openConnection();

				httpURLConnection.setConnectTimeout(100000);//接続タイムアウトを設定する。
				httpURLConnection.setReadTimeout(100000);//レスポンスデータ読み取りタイムアウトを設定する。
				httpURLConnection.setRequestProperty("User-Agent", "Mediahinge");// ヘッダを設定
				httpURLConnection.setRequestProperty("Accept-Language", "ja");// ヘッダを設定
				httpURLConnection.addRequestProperty("Content-Type", "application/json; charset=UTF-8");
				httpURLConnection.setRequestMethod("POST");//HTTPのメソッドをPOSTに設定する。
				httpURLConnection.setDoOutput(true);//リクエストのボディ送信を許可する
				httpURLConnection.setDoInput(true);//レスポンスのボディ受信を許可する
				
				//コネクションを開く
				httpURLConnection.connect();
				
				//リクエストボディの書き出しを行う
				OutputStream outputStream = httpURLConnection.getOutputStream();//OutputStreamを取得

				String id = articleForm0.get_id();
				
				JSONObject json = new JSONObject();//POSTするJSONオブジェクト
				json.put("_id",id);
				
				PrintStream printStream = new PrintStream(outputStream);
				printStream.print(json.toString());//データをPOSTする
				printStream.close();
				
				outputStream.close();

				//レスポンスボディの読み出しを行う。
				InputStream inputStream = httpURLConnection.getInputStream();
				BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
				StringBuffer stringBuffer = new StringBuffer();
				String line = "";
				while ((line = bufferedReader.readLine()) != null) {
					stringBuffer.append(line);
				}
				bufferedReader.close();
				inputStream.close();

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
				
				ArticleForm articleForm1 = articleService.get(rateForm.get_id1());
				Thread.sleep(400);
				if(articleForm1.getHighest_rate() <= rateForm.getRate1()) {
					articleForm0.setHighest_rate(rateForm.getRate1());
					articleForm1.setHighest_rate(rateForm.getRate1());
					articleForm0.setTopics_id(topics_id);
					articleForm1.setTopics_id(topics_id);
				}
				ArticleForm articleForm2 = articleService.get(rateForm.get_id2());
				Thread.sleep(400);
				if(articleForm2.getHighest_rate() <= rateForm.getRate2()) {
					articleForm0.setHighest_rate(rateForm.getRate2());
					articleForm2.setHighest_rate(rateForm.getRate2());
					articleForm0.setTopics_id(topics_id);
					articleForm2.setTopics_id(topics_id);
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
