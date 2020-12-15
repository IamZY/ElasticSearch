package com.ntuzy.utils;

import com.ntuzy.pojo.Content;
import com.ntuzy.service.ContentService;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

@Component
public class HtmlParseUtil {


    public List<Content> parseJD(String keywords) throws IOException {
        // 获取请求
        // ajax 不能获取
        String url = "https://search.jd.com/Search?keyword=" + keywords;

        // 解析网页 返回的document 就是 Document对象
        Document document = Jsoup.parse(new URL(url), 30000);
        Element element = document.getElementById("J_goodsList");

        // 获取所有的li元素
        Elements elements = element.getElementsByTag("li");
        ArrayList<Content> goodsList = new ArrayList<>();
        for (Element e : elements) {
            String img = e.getElementsByTag("img").eq(0).attr("data-lazy-img");
            String price = e.getElementsByClass("p-price").eq(0).text();
            String title = e.getElementsByClass("p-name").eq(0).text();

//            System.out.println("=======================================================");
//            System.out.println(img);
//            System.out.println(price);
//            System.out.println(title);

            goodsList.add(new Content(title, img, price));
        }

        return goodsList;

    }


    public static void main(String[] args) throws IOException {
//        List<Content> goodsList = new HtmlParseUtil().parseJD("java");
//
//        for (Content content : goodsList) {
//            System.out.println(content);
//        }

        new ContentService().parseContent("java");


    }
}
