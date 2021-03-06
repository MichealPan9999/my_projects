package com.ktc.tvlauncher.tencent.bean;

import java.util.Arrays;

public class TencentMoviesReInfo {
	private String[] title;
    private String[] pic_470_630;
    private String[] directors;
    private String[] actor;
    private String[] uri;
    private String[] description;
    private String[] present_year;
    private String[] item_id;
    private String[] s_title;
	public String[] getTitle() {
		return title;
	}
	public void setTitle(String[] title) {
		this.title = title;
	}
	public String[] getPic_470_630() {
		return pic_470_630;
	}
	public void setPic_470_630(String[] pic_470_630) {
		this.pic_470_630 = pic_470_630;
	}
	public String[] getDirectors() {
		return directors;
	}
	public void setDirectors(String[] directors) {
		this.directors = directors;
	}
	public String[] getActor() {
		return actor;
	}
	public void setActor(String[] actor) {
		this.actor = actor;
	}
	public String[] getUri() {
		return uri;
	}
	public void setUri(String[] uri) {
		this.uri = uri;
	}
	public String[] getDescription() {
		return description;
	}
	public void setDescription(String[] description) {
		this.description = description;
	}
	public String[] getPresent_year() {
		return present_year;
	}
	public void setPresent_year(String[] present_year) {
		this.present_year = present_year;
	}
	public String[] getItem_id() {
		return item_id;
	}
	public void setItem_id(String[] item_id) {
		this.item_id = item_id;
	}
	public String[] getS_title() {
		return s_title;
	}
	public void setS_title(String[] s_title) {
		this.s_title = s_title;
	}
	@Override
	public String toString() {
		return "TencentInfo [title=" + Arrays.toString(title)
				+ ", pic_470_630=" + Arrays.toString(pic_470_630)
				+ ", directors=" + Arrays.toString(directors) + ", actor="
				+ Arrays.toString(actor) + ", uri=" + Arrays.toString(uri)
				+ ", description=" + Arrays.toString(description)
				+ ", present_year=" + Arrays.toString(present_year)
				+ ", item_id=" + Arrays.toString(item_id) + ", s_title="
				+ Arrays.toString(s_title) + "]";
	}
}
