package jp.mediahinge.spring.boot.app.get_data;

import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.SocketAddress;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.sun.syndication.feed.synd.SyndEntry;
import com.sun.syndication.feed.synd.SyndFeed;
import com.sun.syndication.io.SyndFeedInput;
import com.sun.syndication.io.XmlReader;

import jp.mediahinge.spring.boot.app.form.RSSForm;
import jp.mediahinge.spring.boot.app.service.CloudantRSSService;

@Component
public class GetRSS {

	@Autowired
	private CloudantRSSService rssService;
	
	public List<RSSForm> insertRSS() throws Exception{

		List<RSSForm> insertedRSS = new ArrayList<>();
		
		String urlstr[] = new String[3];
		urlstr[0] = "https://assets.wor.jp/rss/rdf/yomiuri/politics.rdf";//読売
		urlstr[1] = "http://www3.asahi.com/rss/politics.rdf";//朝日
		urlstr[2] = "https://mainichi.jp/rss/etc/mainichi-flash.rss";//毎日

		System.out.println(urlstr[0]);

		for(int i = 0; i<3; i++) {
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

			RSSForm rssForm = new RSSForm();

			rssForm.setType("rss");
			if (i == 0) {
				rssForm.setMedia("yomiuri");
			}else if(i == 1 ) {
				rssForm.setMedia("asahi");
			}else {
				rssForm.setMedia("mainichi");
			}

			// 記事リンク取得
			for (Object obj : feed.getEntries()) {
				SyndEntry entry = (SyndEntry) obj;

				//記事URLが新規のものであった場合
				if(rssService.findURL(entry.getLink()).size() == 0) {

					rssForm.setUrl(entry.getLink());
					rssService.persist(rssForm);
					System.out.println("Successfully inserted rss data!");
					
					insertedRSS.add(rssForm);

					Thread.sleep(200);
				} 
				//記事URLが既存のものであった場合
				else {
					System.out.println("This rss is pre exists");
					
					//以下２行はテストメソッド
//					rssForm.setUrl(entry.getLink());
//					test_GetArticle(rssForm);
					
					break;
				}
				//entry.getLink()がURL
			}
			System.out.println("Finished reading " + rssForm.getMedia() + "'s rss data\n");

		}
		
		return insertedRSS;
	}
}
