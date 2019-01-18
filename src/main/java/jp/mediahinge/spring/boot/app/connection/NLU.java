package jp.mediahinge.spring.boot.app.connection;

import org.springframework.stereotype.Component;

import com.ibm.watson.developer_cloud.natural_language_understanding.v1.NaturalLanguageUnderstanding;
import com.ibm.watson.developer_cloud.natural_language_understanding.v1.model.AnalysisResults;
import com.ibm.watson.developer_cloud.natural_language_understanding.v1.model.AnalyzeOptions;
import com.ibm.watson.developer_cloud.natural_language_understanding.v1.model.ConceptsOptions;
import com.ibm.watson.developer_cloud.natural_language_understanding.v1.model.EntitiesOptions;
import com.ibm.watson.developer_cloud.natural_language_understanding.v1.model.Features;
import com.ibm.watson.developer_cloud.natural_language_understanding.v1.model.KeywordsOptions;
import com.ibm.watson.developer_cloud.service.security.IamOptions;

import jp.mediahinge.spring.boot.app.form.ArticleForm;
import jp.mediahinge.spring.boot.app.form.NLUForm;
import jp.mediahinge.spring.boot.app.service.CloudantNLUService;

@Component
public class NLU {

	private NLUForm nluForm = new NLUForm();
    
	public void setNLUForm(ArticleForm articleForm) {
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
	}
	public NLUForm getNLUForm() {
		return nluForm;
	}
	
	public void insertAnalysisResults(CloudantNLUService nluService) throws Exception{
		nluService.persist(nluForm);
		Thread.sleep(400);
		
		System.out.println("Successfully inserted Analysis Results!");
	}
}
