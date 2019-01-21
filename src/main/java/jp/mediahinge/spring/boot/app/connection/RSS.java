package jp.mediahinge.spring.boot.app.connection;

import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.SocketAddress;
import java.net.URL;
import java.net.URLConnection;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.springframework.stereotype.Component;

import com.sun.syndication.feed.synd.SyndEntry;
import com.sun.syndication.feed.synd.SyndFeed;
import com.sun.syndication.io.SyndFeedInput;
import com.sun.syndication.io.XmlReader;

import jp.mediahinge.spring.boot.app.form.RSSForm;
import jp.mediahinge.spring.boot.app.schedule.ScheduledMethods;
import jp.mediahinge.spring.boot.app.service.CloudantRSSService;

@Component
public class RSS {

	private List<RSSForm> rssList = new ArrayList<>();

	public void setRSSList(CloudantRSSService rssService) throws Exception{

		String urlstr[] = new String[3];
		urlstr[0] = "https://assets.wor.jp/rss/rdf/yomiuri/politics.rdf";//読売
		urlstr[1] = "http://www3.asahi.com/rss/politics.rdf";//朝日
		urlstr[2] = "https://mainichi.jp/rss/etc/mainichi-flash.rss";//毎日

		for(int i = 0; i<3; i++) {
			String media;
			if (i == 0) {
				media = "yomiuri";
			}else if(i == 1 ) {
				media = "asahi";
			}else {
				media = "mainichi";
			}

			SyndFeedInput input = new SyndFeedInput();
			URL url = new URL(urlstr[i]);
			URLConnection urlConnection;

			//以下三行はローカル実行時にのみ必要な記述、デプロイ時にコメントアウト必須
//			SocketAddress addr = new InetSocketAddress("172.17.0.2", 80);
//			Proxy proxy = new Proxy(Proxy.Type.HTTP,addr);
//			urlConnection = url.openConnection(proxy);

			urlConnection = url.openConnection();

			urlConnection.setRequestProperty("User-Agent","Mozilla/5.0");

			SyndFeed feed = input.build(new XmlReader(urlConnection));

			System.out.println("Started reading " + media + "'s rss data");
			
			System.out.println(rssService);
			
			//記事リンク取得
			for (Object obj : feed.getEntries()) {
				SyndEntry entry = (SyndEntry) obj;
				
				RSSForm rssForm = new RSSForm();
				rssForm.setType("rss");
				if (i == 0) {
					rssForm.setMedia("yomiuri");
				}else if(i == 1 ) {
					rssForm.setMedia("asahi");
				}else {
					rssForm.setMedia("mainichi");
				}
				
				//entry.getLink()がURL
				String notContainQueryString = entry.getLink();
				if(notContainQueryString.indexOf("?") != -1) {
					notContainQueryString = notContainQueryString.substring(0, entry.getLink().indexOf("?"));
				}
				rssForm.setUrl(notContainQueryString);
				
				Date today = new Date();
				SimpleDateFormat id_format = new SimpleDateFormat("yyyy/MM/dd/");
				if(ScheduledMethods.getRSSCounter() < 10) {
					rssForm.set_id("R" + id_format.format(today) + "00" + ScheduledMethods.getRSSCounter());
				}
				else if(ScheduledMethods.getRSSCounter() < 100) {
					rssForm.set_id("R" + id_format.format(today) + "0" + ScheduledMethods.getRSSCounter());
				}
				else {
					rssForm.set_id("R" + id_format.format(today) + ScheduledMethods.getRSSCounter());
				}

				//記事URLが新規のものであった場合
				int result = rssService.searchURL(notContainQueryString).size();
				Thread.sleep(400);

				if(result == 0) {
					rssList.add(rssForm);
					ScheduledMethods.incrementRSSCounter();
				}
				//記事URLが既存のものであった場合
				else {
					break;
				}
			}
			System.out.println("Finished reading " + media + "'s rss data!\n");
		}

	}

	public List<RSSForm> getRSSList(){
		return rssList;
	}

	public void insertRSS(CloudantRSSService rssService) throws Exception{

		System.out.println("Started inserting rss data!");

		for(Object obj :rssList) {
			RSSForm rssForm = (RSSForm)obj;
			rssService.persist(rssForm);
			Thread.sleep(400);
		}

		System.out.println("Successfully inserted rss data!");
	}
}
