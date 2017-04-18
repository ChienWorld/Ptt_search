package com.ptt;

import java.sql.ResultSet;
import java.sql.Connection;

import org.apache.lucene.search.ReferenceManager;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.index.IndexWriter;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.IntPoint;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;

public class Write
{
    private ReferenceManager<IndexSearcher> searcherManager = null;
    private IndexWriter                     indexWriter     = null;
    private Connection                      connection      = null;

    public Write(ReferenceManager<IndexSearcher> searcherManager, IndexWriter indexWriter, Connection connection)
    {
        this.searcherManager = searcherManager;
        this.indexWriter = indexWriter;
        this.connection = connection;
    }

    public void write()
    {
        Runnable task = () -> {
            try {
                ResultSet result = Sqlite.execStmt(connection, "select title, pdate, author, rating, href from PTT;");
                while(result.next())
                {
                    String title  = result.getString(1);
                    String pdate  = result.getString(2);
                    String author = result.getString(3);
                    int rating    = result.getInt(4);
                    String href   = result.getString(5);
                    addDoc(title, pdate, author, rating, href);
                    searcherManager.maybeRefresh();
                }
            } catch(Exception e) {
                e.printStackTrace();
            }
        };
        Thread writeThread = new Thread(task);
        writeThread.setDaemon(true);
        writeThread.start();
    }

    public void addDoc(String title, String pdate, String author, int rating, String href)
        throws java.io.IOException
    {
        Document doc = new Document();
        doc.add(new TextField("title", title, Field.Store.YES));
        doc.add(new StringField("pdate", pdate, Field.Store.YES));
        doc.add(new StringField("author", author, Field.Store.YES));
        doc.add(new IntPoint("rating", rating));
        doc.add(new StringField("href", href, Field.Store.YES));
        indexWriter.addDocument(doc);
    }

}
