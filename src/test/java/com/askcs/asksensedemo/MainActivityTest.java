package com.askcs.asksensedemo;

import com.xtremelabs.robolectric.RobolectricTestRunner;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

@RunWith(RobolectricTestRunner.class)
public class MainActivityTest {

    @Test
    public void testAppName() throws Exception {
        String appName = new MainActivity().getResources().getString(R.string.app_name);
        assertThat(appName, equalTo("Ask Sense Demo"));
    }
}
