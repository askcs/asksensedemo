package com.askcs.asksensedemo.util;

import com.xtremelabs.robolectric.RobolectricTestRunner;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

@RunWith(RobolectricTestRunner.class)
public class UtilsTest {

    @Test
    public void md5Test() {

        // Used: http://www.md5.cz/

        assertThat(Utils.md5("123"), is("202cb962ac59075b964b07152d234b70"));

        assertThat(Utils.md5("tEst h@sh"), is("6c01a47a8a4fd6e6240dd7d50dd65720"));

        assertThat(Utils.md5("!@#$"), is("3a4d92a1200aad406ac50377c7d863aa"));

        assertThat(Utils.md5(""), is("d41d8cd98f00b204e9800998ecf8427e"));
    }

    @Test(expected = NullPointerException.class)
    public void md5FailTest() {

        Utils.md5(null);
    }
}
