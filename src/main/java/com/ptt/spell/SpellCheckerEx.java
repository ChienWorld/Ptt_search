package com.ptt.spell;

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.spell.LuceneDictionary;
import org.apache.lucene.search.spell.SpellChecker;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.cn.smart.SmartChineseAnalyzer;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.analysis.th.ThaiAnalyzer;
import java.io.IOException;
import java.nio.file.Paths;
import java.nio.file.Path;
import java.util.Scanner;

public class SpellCheckerEx
{
    private final static String home = System.getProperty("user.dir");

    public static void main(String[] args)
        throws IOException
    {
        String indexPath = home + "/ptt/index2";
        String spellPath = home + "/ptt/spell";

        buildSpellCheckerIndexDir(indexPath, spellPath);
        
        Directory spellCheckDir = FSDirectory.open(Paths.get(spellPath));
        Directory indexDir = FSDirectory.open(Paths.get(indexPath));

        Scanner scanner = new Scanner(System.in);
        String query = null;
        while((query = scanner.next())!=":q") {
            String[] similarties = doSpellCheck(query, 10, indexDir, spellCheckDir);
            System.out.println(similarties.length);
            if(similarties == null) {
                System.out.println("no fitup similarties.");
                continue;
            }
            for(int i=0; i<similarties.length; i++) {
                System.out.println(String.format("%d -> %s", i, similarties[i]));
            }
            System.out.println("\n=================================\n");
        }

    }

    /**
     * build spell checker index dir
     * 
     * @param indexPath
     * @param spellPath
     * 
     * @throws IOException
     * */
    public static void buildSpellCheckerIndexDir(String indexPath, String spellPath)
        throws IOException
    {
        Directory indexDir = FSDirectory.open(Paths.get(indexPath));
        Directory spellCheckDir = FSDirectory.open(Paths.get(spellPath));
        if(!DirectoryReader.indexExists(spellCheckDir))
        {
            System.out.println("\nError: No spellchecker index at path=" + spellPath);
            System.out.println("Now, we create it.");
            _createSpellCheckDir(indexDir, spellCheckDir);
        }
        indexDir.close();
        spellCheckDir.close(); 
    }

    /**
     * create spell check index dir
     *
     * @param spellCheckDir
     * @param indexDir
     *
     * @throws IOException
     * */
    public static void _createSpellCheckDir(Directory indexDir, Directory spellCheckDir)
        throws IOException
    {
        SpellChecker spellChecker = new SpellChecker(spellCheckDir);
        DirectoryReader indexReader = DirectoryReader.open(indexDir);
        //Analyzer analyzer = new SmartChineseAnalyzer(true);
        Analyzer analyzer = new ThaiAnalyzer();
        LuceneDictionary luceneDict = new LuceneDictionary(indexReader, "title");
        IndexWriterConfig indexConfig = new IndexWriterConfig(analyzer);
        spellChecker.indexDictionary(luceneDict, indexConfig, true); 
        spellChecker.close();
        indexReader.close();
        System.out.println("建立完成");
    }

    /**
     * do spell checking
     * 
     * @param query
     * @param suggestNumber
     * @param indexDir
     * @param spellCheckDir
     * 
     * @return similar string
     * */
    public static String[] doSpellCheck(String query, int suggestNumber, Directory indexDir, Directory spellCheckDir)
    {
        String[] similarties = null;
        try {
            SpellChecker spellChecker = new SpellChecker(spellCheckDir);
            spellChecker.setAccuracy(0.25f);
            DirectoryReader indexReader = DirectoryReader.open(indexDir);
            similarties = spellChecker.suggestSimilar(query, suggestNumber);
        } catch(IOException e) {
            e.printStackTrace();
        }
        return similarties;
    }


}
