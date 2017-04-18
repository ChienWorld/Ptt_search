package com.ptt;

import java.util.Arrays;

import java.sql.Connection;
import java.sql.PreparedStatement;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import org.apache.lucene.search.ReferenceManager;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.index.IndexWriter;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Crawler
{
    private ReferenceManager<IndexSearcher> searcherManager = null;
    private IndexWriter indexWriter = null;
    private Write writer = null;
    private Connection connection = null;
    private int pageTotal = 22677; 
    private int pageId = 0;


    public Crawler(ReferenceManager<IndexSearcher> searcherManager, IndexWriter indexWriter, Connection connection)
    {
        this.searcherManager = searcherManager;
        this.indexWriter = indexWriter;
        this.connection = connection;
        writer = new Write(searcherManager, indexWriter, connection);
    }

    public int getCurrentPageId()
    {
        return pageId;
    }

    public void setCrawlingPageNums(int pageTotal)
    {
        this.pageTotal = pageTotal;
    }

    public void crawl()
    {

        Runnable crawlTask = ()->{
            try {
                _crawl();
            } catch(Exception e) {
                e.printStackTrace();
            }
        };

        Thread crawlThread = new Thread(crawlTask);
        crawlThread.setDaemon(true);
        crawlThread.start();
    }


    private void _crawl()
        throws java.sql.SQLException, java.io.IOException
    {
        PreparedStatement pStmt = Sqlite.getInsertPStmt(connection);

        for(pageId = 1 ; pageId <= pageTotal ; pageId++) {
            org.jsoup.nodes.Document doc = Jsoup.connect(String.format("https://www.ptt.cc/bbs/Gossiping/index%d.html", pageId))
                .cookie("over18", "1")
                .get();

            Elements titles = doc.select("div.title > a[href]");
            Elements pdates = doc.select("div.date");
            Elements authors = doc.select("div.author");
            Elements ratings = doc.select("div.nrec");

            for(int index = 0 ; index < titles.size() ; index++)
            {
                String title = titles.get(index).text();
                String author = authors.get(index).text();

                String rating = ratings.get(index).text();
                rating = (rating.equals("爆"))?"100":rating;
                rating = (rating.isEmpty() || rating.contains("X"))?"0":rating;

                String href = titles.get(index).attr("abs:href");
                String pdate = fetchDate(href);
                //System.out.println(title + "," + rating + "," + href + "," + pageId);
                Sqlite.execPStmt(connection, pStmt, Arrays.asList(new Object[]{title, pdate, author, Integer.valueOf(rating), href}));
                writer.addDoc(title, pdate, author, Integer.valueOf(rating), href);
                searcherManager.maybeRefresh();
            }
        }
    }

    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
    private final String  RULES    = "[A-Z]\\.([0-9]+)\\.[A-Z]\\.([A-F0-9]+)\\.html?";
    private final Pattern pattern  = Pattern.compile(RULES);

    private String fetchDate(String href)
    {
        Matcher matcher = pattern.matcher(href);
        boolean found = matcher.find();
        if(found) {
            String millis = matcher.group(1) + (Long.parseLong(matcher.group(2), 16)/10);
            return dateFormat.format(new Date(Long.valueOf(millis)));
        }
        return dateFormat.format(new Date(0));
    }

}
