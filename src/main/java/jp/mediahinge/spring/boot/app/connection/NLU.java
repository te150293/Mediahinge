package jp.mediahinge.spring.boot.app.connection;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.springframework.stereotype.Component;

import com.ibm.watson.developer_cloud.natural_language_understanding.v1.NaturalLanguageUnderstanding;
import com.ibm.watson.developer_cloud.natural_language_understanding.v1.model.AnalysisResults;
import com.ibm.watson.developer_cloud.natural_language_understanding.v1.model.AnalyzeOptions;
import com.ibm.watson.developer_cloud.natural_language_understanding.v1.model.ConceptsOptions;
import com.ibm.watson.developer_cloud.natural_language_understanding.v1.model.ConceptsResult;
import com.ibm.watson.developer_cloud.natural_language_understanding.v1.model.EntitiesOptions;
import com.ibm.watson.developer_cloud.natural_language_understanding.v1.model.EntitiesResult;
import com.ibm.watson.developer_cloud.natural_language_understanding.v1.model.Features;
import com.ibm.watson.developer_cloud.natural_language_understanding.v1.model.KeywordsOptions;
import com.ibm.watson.developer_cloud.service.security.IamOptions;

import jp.mediahinge.spring.boot.app.bean.ArticleBean;
import jp.mediahinge.spring.boot.app.bean.NLUBean;
import jp.mediahinge.spring.boot.app.service.NLUService;

@Component
public class NLU {

	private NLUBean nluForm = new NLUBean();

	public void setNLUForm(ArticleBean articleForm, NLUBean onlyPythonResults) {
		String language = "ja";

		KeywordsOptions keywords = new KeywordsOptions.Builder().build();
		EntitiesOptions entities = new EntitiesOptions.Builder().build();
		ConceptsOptions concepts = new ConceptsOptions.Builder().build();

		Features features = new Features.Builder()
				.keywords(keywords)
				.entities(entities)
				.concepts(concepts)
				.build();
		
		AnalyzeOptions parameters = new AnalyzeOptions.Builder()
				.features(features)
				.language(language)
				.text(articleForm.getText())
				.build();

		IamOptions options = new IamOptions.Builder()
				.apiKey("QaOl-WQtrefrhpHZy1XIknYvIom8WpraljZNt2Jcx-FJ")
				.build();

		NaturalLanguageUnderstanding nlu = new NaturalLanguageUnderstanding("2018-03-16", options);

		AnalysisResults response;

		response = nlu
				.analyze(parameters)
				.execute();
		
		nluForm.setType("Analysis Results");
		nluForm.setArticle_id(articleForm.get_id());
		nluForm.setConcepts(response.getConcepts());
		nluForm.setEntities(response.getEntities());
		nluForm.setKeywords(response.getKeywords());
		
		//Pythonの解析結果とNLUの解析結果を結合
		//Pythonの解析結果がnullでない場合
		if(onlyPythonResults != null) {
			//最終的にsetするresultsのリスト
			List<String> combinedResults = onlyPythonResults.getResults();
			//Entities
			List<EntitiesResult> entitiesList = nluForm.getEntities();
			for(Object obj : entitiesList) {
				EntitiesResult entity = (EntitiesResult) obj;
				if(!Arrays.asList(combinedResults).contains(entity.getText())) {
					combinedResults.add(entity.getText());//resultsと重複していないものをリストに追加
				}
			}
			
			nluForm.setResults(combinedResults);
		}
		else {
			List<String> notSet = new ArrayList<>();
			notSet.add("null");
			nluForm.setResults(notSet);
		}
	}
	public NLUBean getNLUForm() {
		return nluForm;
	}
	
	public List<ConceptsResult> getConceptsResults(){
		return nluForm.getConcepts();
	}

	public List<EntitiesResult> getEntitiesResult(){
		return nluForm.getEntities();
	}
	
	public void insertAnalysisResults(NLUService nluService) throws Exception{
		nluService.persist(nluForm);
		Thread.sleep(400);
		
		System.out.println("Successfully inserted Analysis Results!");
	}
}
