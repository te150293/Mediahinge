package jp.mediahinge.spring.boot.app.bean;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class RSSBean extends BaseBean{
	private String media;
	private String url;
	
}
