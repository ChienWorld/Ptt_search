# Ptt_search
---
This is a free software project, feel free to fork.
Lucene based search engine for searching [PTT](https://www.ptt.cc/bbs/Gossiping/index.html) website. (Gossiping for current)

#### search
![](http://i.imgur.com/zE3ozKG.gif)


#### spellcheck
![](https://media.giphy.com/media/xUA7b0QcnHG8z50SKk/giphy.gif)

### Requirement
* java 8
* maven


### How to run ?
maven run
```
mvn clean compile exec:java -Dexec.mainClass=com.ptt.Search
```
archived run
```
mvn clean compile assembly:single
java -jar target/search-0.1.jar
```

### Environment
* ptt/index :all the lucene data will store here.
* ptt.db    :sqlite to store crawling data with table name == PTT.
 
