package me.yuu.liteadapter;

import android.support.test.runner.AndroidJUnit4;
import android.util.SparseArray;

import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;

/**
 * Instrumented test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
public class ExampleInstrumentedTest {
    @Test
    public void useAppContext() throws Exception {
//        // Context of the app under test.
//        Context appContext = InstrumentationRegistry.getTargetContext();
//
//        assertEquals("me.yuu.liteadapter.test", appContext.getPackageName());

        SparseArray<String> array = new SparseArray<>();
        array.put(10,"10");
        array.put(11,"10");
        array.put(12,"10");
        array.put(13,"10");
        array.put(14,"10");

        assertEquals(0, array.indexOfKey(10));
        assertEquals(1, array.indexOfKey(11));
        assertEquals(2, array.indexOfKey(12));
        assertEquals(3, array.indexOfKey(13));
        assertEquals(4, array.indexOfKey(14));
    }
}
