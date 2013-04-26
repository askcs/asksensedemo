package com.askcs.asksensedemo;

import com.xtremelabs.robolectric.RobolectricTestRunner;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertThat;

@RunWith(RobolectricTestRunner.class)
public class RegisterActivityTest {

    @Test
    public void notNullTest() throws Exception {

        RegisterActivity activity = new RegisterActivity();

        activity.onCreate(null);

        assertThat(activity, not(equalTo(null)));
    }
}
