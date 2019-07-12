package pipeline.items;

import annotation.mysql.Column;
import annotation.mysql.Table;
import pipeline.Item;

/**
 * @Author: zhaoyoucheng
 * @Date: 2019/7/10 13:36
 * @Description:
 */

@Table("toutiao_article")
public class ToutiaoPO implements Item {

    @Column
    private Integer id;
    @Column(name = "title")
    private String title;
    @Column(name = "author")
    private String author;
    @Column(name = "content")
    private String content;
    @Column(name = "poster")
    private String poster;
    @Column(name = "publishTime")
    private Long publishTime;
    @Column(name = "isOriginal")
    private Boolean isOriginal;
    @Column(name = "url")
    private String url;

    public String getPoster() {
        return poster;
    }

    public void setPoster(String poster) {
        this.poster = poster;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Long getPublishTime() {
        return publishTime;
    }

    public void setPublishTime(Long publishTime) {
        this.publishTime = publishTime;
    }

    public Boolean getOriginal() {
        return isOriginal;
    }

    public void setOriginal(Boolean original) {
        isOriginal = original;
    }
}