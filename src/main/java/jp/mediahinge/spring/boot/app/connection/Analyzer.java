package jp.mediahinge.spring.boot.app.connection;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.HttpURLConnection;
import java.net.URL;

import org.json.JSONObject;
/**
 * 
 * @author 150293
 *
 */
public class Analyzer {
	/**
	 * Analyzerへのconnectionの設定を行う
	 * 
	 * @param httpURLConnection
	 * @param urlString
	 * @return HttpURLConnection
	 * @throws Exception
	 */
	public static HttpURLConnection connectAnalyzer(HttpURLConnection httpURLConnection,String urlString) throws Exception{
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
		
		return httpURLConnection;
	}
	
	/**
	 * ResponseBody の読み出しを行う
	 * 
	 * @param httpURLConnection
	 * @return StringBuffer
	 * @throws Exception
	 */
	public static StringBuffer getResponceBody(HttpURLConnection httpURLConnection) throws Exception{
		InputStream inputStream = httpURLConnection.getInputStream();
		BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
		StringBuffer stringBuffer = new StringBuffer();
		String line = "";
		while ((line = bufferedReader.readLine()) != null) {
			stringBuffer.append(line);
		}
		bufferedReader.close();
		inputStream.close();
		
		return stringBuffer;
	}
	
	public static void postJson(OutputStream outputStream, JSONObject json) throws Exception{
		//データをPOSTする
		PrintStream printStream = new PrintStream(outputStream);
		printStream.print(json.toString());
		
		printStream.close();
		outputStream.close();
		
	}
}
