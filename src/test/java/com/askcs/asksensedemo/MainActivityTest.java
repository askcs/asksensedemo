package com.askcs.asksensedemo;

import com.xtremelabs.robolectric.RobolectricTestRunner;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertThat;

@RunWith(RobolectricTestRunner.class)
public class MainActivityTest {

    @Test
    public void notNullTest() throws Exception {

        MainActivity activity = new MainActivity();

        activity.onCreate(null);

        assertThat(activity, not(equalTo(null)));
    }
}
