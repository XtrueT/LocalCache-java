import org.junit.Test;

import java.util.concurrent.TimeUnit;

import static org.junit.Assert.*;

/**
 * @Author: 霁
 * @Date: 2020/7/8 12:48
 */
public class TestLocalCache {

    @Test
    public void testLocalCache() throws InterruptedException {
        int size = 10;
        LocalCache.setDefaultSize(size);
        long timeout = 36 * 1000;
        LocalCache.setDefaultTimeout(timeout);
        LocalCache localCache = LocalCache.getInstance();

        // 测试 设置缓存大小与默认失效时间
        assertNotNull(localCache);
        assertEquals(size,LocalCache.getDefaultSize());
        assertEquals(timeout,LocalCache.getDefaultTimeout());

        // 测试 set get
        String test_key = "test_key";
        String test_value = "test_value";
        localCache.set(test_key,test_value);
        Object o = localCache.get(test_key);
        assertEquals(test_value,o);

        // 测试移除
        localCache.remove(test_key);
        assertFalse(LocalCache.getConcurrentHashMap().containsKey(test_key));

        // 测试 内存回收
        localCache.set(test_key,test_value,-1);
        for(int i=1;i<=10;i++){
            localCache.set("i_"+i,i);
        }
        assertFalse(LocalCache.getConcurrentHashMap().containsKey(test_key));

        // 测试清空
        localCache.clear();
        assertEquals(0,LocalCache.getConcurrentHashMap().size());

        // 测试不失效
        localCache.set(test_key,test_value,-1);
        TimeUnit.SECONDS.sleep(40);
        assertTrue(LocalCache.getConcurrentHashMap().containsKey(test_key));
    }
}
