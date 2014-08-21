package com.chainway.ht.bean;

import com.ab.db.orm.annotation.Column;
import com.ab.db.orm.annotation.Id;
import com.ab.db.orm.annotation.Table;

@Table(name = "Fingerprint")
public class Fingerprint extends Base {
	// ID @Id主键,int类型,数据库建表时此字段会设为自增长
	@Id
	@Column(name = "_id")
	private int _id;
	@Column(name = "pageId", type = "INTEGER")
	private int pageId;
	@Column(name = "name")
	private String name;
	@Column(name = "features")
	private String features;
	// 创建时间
	@Column(name = "create_time")
	private String createTime;

	public int get_id() {
		return _id;
	}

	public void set_id(int _id) {
		this._id = _id;
	}

	public String getCreateTime() {
		return createTime;
	}

	public void setCreateTime(String createTime) {
		this.createTime = createTime;
	}

	public String getFeatures() {
		return features;
	}

	public void setFeatures(String features) {
		this.features = features;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getPageId() {
		return pageId;
	}

	public void setPageId(int pageId) {
		this.pageId = pageId;
	}
}
