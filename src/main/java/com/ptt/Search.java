package com.ptt;

import org.apache.lucene.index.Term;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.document.Document;
import org.apache.lucene.search.ControlledRealTimeReopenThread;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ReferenceManager;
import org.apache.lucene.search.SearcherManager;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.cn.smart.SmartChineseAnalyzer;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.store.SimpleFSDirectory;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.sql.Connection;

public class Search
{
    private final static Path indexDirPath   = Paths.get(System.getProperty("user.dir") + "/ptt/index");
    private static Connection connection     = null;
    private static Analyzer analyzer         = null;
    private static Directory indexDir        = null;
    private static IndexWriter indexWriter   = null;
    private static ReferenceManager<IndexSearcher> searcherManager               = null;
    private static ControlledRealTimeReopenThread<IndexSearcher> nrtReopenThread = null;
    private static IndexSearcher indexSearcher = null;
    private static Crawler crawler             = null;
    
    public static void main(String[] args)
        throws Exception
    {
        setUp();
        crawler = new Crawler(searcherManager, indexWriter, connection);
        crawler.crawl();
        search();
    }

    private static void setUp()
        throws java.io.IOException, java.sql.SQLException 
    {
        indexDir                      = FSDirectory.open(indexDirPath);
        //indexDir                      = new RAMDirectory();
        analyzer                      = new SmartChineseAnalyzer(true);
        IndexWriterConfig indexConfig = new IndexWriterConfig(analyzer);
        indexWriter                   = new IndexWriter(indexDir, indexConfig);
        searcherManager               = new SearcherManager(indexWriter, true, true, null);
        nrtReopenThread               = new ControlledRealTimeReopenThread<IndexSearcher>(indexWriter, searcherManager, 1.0, 0.1);
        nrtReopenThread.setName("PTT NRT reopen thread");
        nrtReopenThread.setPriority(Math.min(Thread.currentThread().getPriority() + 2, Thread.MAX_PRIORITY));
        nrtReopenThread.setDaemon(true);
        nrtReopenThread.start();

        // db connection
        connection = Sqlite.getConnection();
        Sqlite.create(connection);
    }

    private static void search()
        throws java.io.IOException, org.apache.lucene.queryparser.classic.ParseException
    {
        BufferedReader buf = new BufferedReader(new InputStreamReader(System.in));
        String keyword = null;

        while(!(keyword = buf.readLine()).equals(":q")){
            System.out.println(String.format("查詢中(%d筆)", crawler.getCurrentPageId()));
            Query query = new QueryParser("title", analyzer).parse(keyword);
            indexSearcher = searcherManager.acquire();
            TopDocs docs = indexSearcher.search(query, 10);
            for(ScoreDoc sdoc : docs.scoreDocs)
            {
                int docId = sdoc.doc;
                Document document = indexSearcher.doc(docId);
                System.out.println(String.format("id=%d, score=%.5f, title=%s, href=%s, date=%s", 
                            docId, sdoc.score, document.get("title"), document.get("href"), document.get("pdate")));
            }
            System.out.println("Found " + docs.totalHits);
            System.out.println("\n=================================================\n");
        }
        searcherManager.release(indexSearcher);
        System.out.println("結束查詢");
    }

}
