package com.thrd.testlucene

import com.thrd.testlucene.LocalSearchManager.indexFile
import com.thrd.testlucene.LocalSearchManager.searchInIndex
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment


/**
 * @author thrd
 * @version 12.05.18.
 */
@RunWith(RobolectricTestRunner::class)
class LuceneTest {
    @Test
    fun testLucene() {
        val fileName = "test_book.epub"
        indexFile(RuntimeEnvironment.application, fileName)
        val searchResult = searchInIndex("test")
        Assert.assertEquals(4, searchResult.size)
    }
}
