package jp.mediahinge.spring.boot.app.bean;

import lombok.Data;

/**
 * Cloudantに挿入するデータのForm
 */
@Data
public class BaseBean {

	private String _id;
	private String _rev;
	private String type;
}
