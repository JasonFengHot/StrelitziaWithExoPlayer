package tv.ismar.app;

import android.content.Context;
import android.content.Intent;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

import tv.ismar.app.update.UpdateService;

import static junit.framework.TestCase.assertEquals;

@RunWith(AndroidJUnit4.class)
public class ExampleInstrumentedTest {
    @Test
    public void useAppContext() throws Exception {
        // Context of the app under test.
        Context appContext = InstrumentationRegistry.getTargetContext();

        assertEquals("tv.ismar.statistics.test", appContext.getPackageName());
    }

    public void testAppUpdate(){
        Context appContext = InstrumentationRegistry.getTargetContext();
    }
}
