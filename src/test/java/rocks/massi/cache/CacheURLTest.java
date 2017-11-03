package rocks.massi.cache;

import org.junit.Test;

import static org.junit.Assert.*;

public class CacheURLTest {
    @Test
    public void getOriginal() throws Exception {
        CacheURL cacheURL = new CacheURL("file:///tmp/test.txt");
        assertEquals("file:///tmp/test.txt", cacheURL.getOriginal());
    }

    @Test(expected = StringIndexOutOfBoundsException.class)
    public void getProtocol() throws Exception {
        assertEquals("redis", new CacheURL("redis://server/path?param=1&otherparam=2:").getProtocol());
        assertEquals("my_very-own?protocol", new CacheURL("my_very-own?protocol://path").getProtocol());
        assertEquals("postgres", new CacheURL("postgres://my-server://even-if-bad").getProtocol());
        assertEquals("file", new CacheURL("file:///tmp/test.txt").getProtocol());

        // This will throw a StringIndexOutOfBounds.
        new CacheURL("no-protocol/on/this/one");
    }

    @Test
    public void getPath() throws Exception {
        assertEquals("/tmp/file.cache", new CacheURL("protocol:///tmp/file.cache").getPath());
        assertEquals("localhost?password=hello&user=user",
                new CacheURL("protocol://localhost?password=hello&user=user").getPath());
    }

}