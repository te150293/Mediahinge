package jp.mediahinge.spring.boot.app.bean;

import java.util.List;

import com.ibm.watson.developer_cloud.natural_language_understanding.v1.model.ConceptsResult;
import com.ibm.watson.developer_cloud.natural_language_understanding.v1.model.EntitiesResult;
import com.ibm.watson.developer_cloud.natural_language_understanding.v1.model.KeywordsResult;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class NLUBean extends BaseBean{
	  private String article_id;
	  private List<ConceptsResult> concepts;
	  private List<EntitiesResult> entities;
	  private List<KeywordsResult> keywords;
	  private List<String> results;

}
