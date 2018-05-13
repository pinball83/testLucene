package com.thrd.testlucene

import org.apache.lucene.analysis.standard.StandardAnalyzer
import org.apache.lucene.document.Document
import org.apache.lucene.document.Field
import org.apache.lucene.document.Field.Index
import org.apache.lucene.document.Field.Store
import org.apache.lucene.index.IndexReader
import org.apache.lucene.index.IndexWriter
import org.apache.lucene.index.IndexWriterConfig
import org.apache.lucene.queryParser.ParseException
import org.apache.lucene.queryParser.QueryParser
import org.apache.lucene.search.IndexSearcher
import org.apache.lucene.search.Query
import org.apache.lucene.search.TopScoreDocCollector
import org.apache.lucene.store.Directory
import org.apache.lucene.store.RAMDirectory
import org.apache.lucene.util.Version
import org.apache.tika.exception.TikaException
import org.apache.tika.metadata.Metadata
import org.apache.tika.parser.ParseContext
import org.apache.tika.parser.epub.EpubParser
import org.apache.tika.sax.BodyContentHandler
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.xml.sax.SAXException
import java.io.IOException


/**
 * @author thrd
 * @version 12.05.18.
 */
@RunWith(RobolectricTestRunner::class)
class LuceneTest {
    @Test
    fun testLucene() {
        val directory = RAMDirectory()
        val fileName = "test_book.epub"
        indexFile(fileName, directory)
        searchIndex("man", directory)
    }

    @Throws(IOException::class)
    fun indexFile(file: String, directory: Directory) {
        val analyzer = StandardAnalyzer(Version.LUCENE_36)

        val config = IndexWriterConfig(Version.LUCENE_36, analyzer)
        val writer = IndexWriter(directory, config)
        writer.deleteAll()

        val metadata = Metadata()
        val handler = BodyContentHandler(10 * 1024 * 1024)
        val context = ParseContext()
//        val parser = AutoDetectParser()
        val parser = EpubParser()
        val stream = RuntimeEnvironment.application.assets.open(file)
//        val stream = FileInputStream(file) //open stream

        try {
            parser.parse(stream, handler, metadata, context) //parse the stream
        } catch (e: TikaException) {
            e.printStackTrace()
        } catch (e: SAXException) {
            e.printStackTrace()
        } finally {
            stream.close() //close the stream
        }

        val text = handler.toString()
        val fileName = file



//        for (key in metadata.names()) {
//            val name = key.toLowerCase()
//            val value = metadata.get(key)
//
//            if (value.isBlank()) {
//                continue
//            }
//
//            when {
//                "keywords".equals(key, ignoreCase = true) ->
//                    for (keyword in value
//                        .split(",?(\\s+)"
//                            .toRegex())
//                        .dropLastWhile { it.isEmpty() }
//                        .toTypedArray()
//                    ) {
//                        doc.add(
//                            Field(
//                                name,
//                                keyword,
//                                Store.YES,
//                                Index.NOT_ANALYZED
//                            ))
//                    }
//                "title".equals(key, ignoreCase = true) ->
//                    doc.add(
//                        Field(
//                            name,
//                            value,
//                            Store.YES,
//                            Index.ANALYZED
//                        ))
//                else -> doc.add(
//                    Field(
//                        name,
//                        fileName,
//                        Store.YES,
//                        Index.NOT_ANALYZED
//                    ))
//            }
//        }
        text.split("\n")
            .filter { it.isNotEmpty() || it.isNotBlank() }
            .forEach {
                val doc = Document()
                doc.add(Field("file", fileName, Store.YES, Index.NO))
                doc.add(
                    Field(
                        "cntnt",
                        it,
                        Store.YES,
                        Index.ANALYZED,
                        Field.TermVector.WITH_POSITIONS_OFFSETS
                    ))
                writer.addDocument(doc)
            }


        writer.commit()
        writer.close(true)
//        writer.deleteUnusedFiles()
    }

    fun searchIndex(keyword: String, index: Directory) {
//        val indexDir = File(WriteIndex.INDEX_DIRECTORY)

//        val index = FSDirectory.open(indexDir)

        // Build a Query object
        val query: Query
        try {
            query = QueryParser(
                Version.LUCENE_36,
                "cntnt",
                StandardAnalyzer(Version.LUCENE_36)
            )
                .parse(keyword)
        } catch (e: ParseException) {
            e.printStackTrace()
            return
        }


        val hitsPerPage = 10
        val reader = IndexReader.open(index)
        val searcher = IndexSearcher(reader)
        val collector = TopScoreDocCollector.create(hitsPerPage, true)
        searcher.search(query, collector)

        println("total hits: " + collector.totalHits)

        val hits = collector.topDocs().scoreDocs
        for (hit in hits) {
            val doc = reader.document(hit.doc)
            println(doc.get("file") + " (${hit.score}) ${doc.get("cntnt")}")
        }
    }
}
