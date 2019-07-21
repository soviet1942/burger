package pipeline;

import annotation.mysql.Column;
import annotation.mysql.Table;

import java.util.Date;

/**
 * @Author: zhaoyoucheng
 * @Date: 2019/7/10 13:36
 * @Description:
 */

@Table("article")
public class ToutiaoPO {

    @Column(name = "id", insertable = false)
    private Integer id;
    @Column(name = "title")
    private String title;
    @Column(name = "author")
    private String author;
    @Column(name = "content")
    private String content;
    @Column(name = "poster")
    private String poster;
    @Column(name = "publish_time")
    private Date publishTime;
    @Column(name = "is_original")
    private Short isOriginal;
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

    public Date getPublishTime() {
        return publishTime;
    }

    public void setPublishTime(Date publishTime) {
        this.publishTime = publishTime;
    }

    public Short getOriginal() {
        return isOriginal;
    }

    public void setOriginal(Short original) {
        isOriginal = original;
    }
}
