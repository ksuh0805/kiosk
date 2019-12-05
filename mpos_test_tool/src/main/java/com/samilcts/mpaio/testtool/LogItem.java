package com.samilcts.mpaio.testtool;

/**
 * Created by mskim on 2015-09-14.
 * mskim@31cts.com
 */
public class LogItem {

    private String detail;

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    private String title;
    private String content;

    public String getSubContent() {
        return subContent;
    }

    public void setSubContent(String subContent) {
        this.subContent = subContent;
    }

    private String subContent;


    public LogItem(String title, String content, String subContent, String detail) {

        this.title = title;
        this.content = content;
        this.subContent = subContent;
        this.detail = detail;
    }

    public String getDetail() {
        return detail;
    }

    public void setDetail(String detail) {
        this.detail = detail;
    }
}