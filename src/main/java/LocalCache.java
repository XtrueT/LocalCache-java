import java.io.Serializable;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;


/**
 * @Author: 霁
 * @Date: 2020/7/7 20:32
 */
public class LocalCache implements Serializable {
    /***
     * 默认缓存过期时间
     */
    private static long DEFAULT_TIMEOUT = 3600 * 1000;
    /***
     * 定时任务执行间隔
     * 单位 秒 不小于 1
     */
    private static final long TASK_TIME = 10;
    /***
     * 缓存空间大小
     */
    private static int DEFAULT_SIZE = 512;
    /***
     * 考虑高并发情况下数据安全使用ConcurrentHashMap
     */
    private static final ConcurrentHashMap<String, CacheEntity> concurrentHashMap = new ConcurrentHashMap<>(DEFAULT_SIZE);

    private static LocalCache localCache = null;

    static ConcurrentHashMap<String, CacheEntity> getConcurrentHashMap() {
        return concurrentHashMap;
    }

    static long getDefaultTimeout() {
        return DEFAULT_TIMEOUT;
    }

    static int getDefaultSize() {
        return DEFAULT_SIZE;
    }

    static void setDefaultTimeout(long defaultTimeout) {
        DEFAULT_TIMEOUT = defaultTimeout;
    }

    static void setDefaultSize(int defaultSize) {
        DEFAULT_SIZE = defaultSize;
    }

    private LocalCache() {
    }

    /***
     * 单例模式
     * @return LocalCache实例
     */
    static LocalCache getInstance() {
        if (localCache == null) {
            localCache = new LocalCache();
            new Thread(new TimeoutTimerThread()).start();
        }
        return localCache;
    }

    /***
     * 设置缓存
     * @param key 键
     * @param value 值
     * @param expire 过期时间
     */
    public synchronized void set(String key, Object value, long expire) throws InterruptedException {
        // 已经存在则更新
        if (concurrentHashMap.containsKey(key)) {
            CacheEntity cacheEntity = concurrentHashMap.get(key);
            cacheEntity.setExpire(expire);
            cacheEntity.setTimeStamp(System.currentTimeMillis());
            cacheEntity.setValue(value);
            return;
        }
        if (localCache.checkIsFull()) {
            concurrentHashMap.remove(fifo());
        }
        TimeUnit.SECONDS.sleep(1);
        concurrentHashMap.put(key, new CacheEntity(key, value, expire));
    }

    /***
     * 设置缓存
     * @param key 键
     * @param value 值
     */
    public synchronized void set(String key, Object value) throws InterruptedException {
        // 已经存在则更新
        if (concurrentHashMap.containsKey(key)) {
            CacheEntity cacheEntity = concurrentHashMap.get(key);
            cacheEntity.setTimeStamp(System.currentTimeMillis());
            cacheEntity.setValue(value);
            System.out.println("更新缓存：" + cacheEntity);
            return;
        }
        if (localCache.checkIsFull()) {
            concurrentHashMap.remove(fifo());
        }
        TimeUnit.SECONDS.sleep(1);
        concurrentHashMap.put(key, new CacheEntity(key, value));
    }

    /***
     * 获取缓存
     * @param key 键
     * @return 返回缓存的值
     */
    public synchronized Object get(String key) {
        CacheEntity cacheEntity = concurrentHashMap.get(key);
        if (cacheEntity == null) {
            return null;
        }
        if (localCache.checkIsExpire(cacheEntity)) {
            concurrentHashMap.remove(key);
            System.out.println("移除失效缓存：" + cacheEntity);
            return null;
        }
        return cacheEntity.getValue();
    }

    /***
     * 移除缓存
     * @param key 键
     */
    public synchronized void remove(String key) {
        concurrentHashMap.remove(key);
    }

    /***
     * 清空缓存
     */
    public void clear() {
        concurrentHashMap.clear();
    }

    /***
     * 检查是否达到最大缓存空间
     * @return true or false
     */
    private boolean checkIsFull() {
        System.out.println("检查是否已达最大缓存：");
        return concurrentHashMap.size() == DEFAULT_SIZE;
    }

    /***
     * 检查是否失效
     * @param cacheEntity 缓存对象
     * @return true or false
     */
    private boolean checkIsExpire(CacheEntity cacheEntity) {
        if (cacheEntity.getExpire() == -1) {
            return false;
        }
        return (System.currentTimeMillis() - cacheEntity.getTimeStamp()) >= cacheEntity.getExpire();
    }

    /***
     * 先进先出
     * @return 应被移除的键
     */
    private String fifo() {
        List<CacheEntity> cacheEntities = new ArrayList<>();
        for (Object k : ((ConcurrentHashMap) LocalCache.concurrentHashMap).keySet()) {
            CacheEntity cacheEntity = (CacheEntity) ((ConcurrentHashMap) LocalCache.concurrentHashMap).get(k);
            cacheEntities.add(cacheEntity);
        }
        cacheEntities.sort((o1, o2) -> Long.compare(o1.getTimeStamp(), o2.getTimeStamp()));
        // System.out.println("排序后缓存列表："+cacheEntities);
        System.out.println("已达最大缓存，移除最先进入的缓存：" + cacheEntities.get(0).getKey());
        return cacheEntities.get(0).getKey();
    }

    /***
     * 定时器线程类
     * 检查缓存过期
     */
    private static class TimeoutTimerThread implements Runnable {

        @Override
        @SuppressWarnings("InfiniteLoopStatement")
        public void run() {
            while (true) {
                try {
                    if (!concurrentHashMap.isEmpty()) {
                        TimeUnit.SECONDS.sleep(TASK_TIME);
                        System.out.println("检查失效缓存：");
                        System.out.println("当前缓存使用" + concurrentHashMap.size() + "剩余" + (DEFAULT_SIZE - concurrentHashMap.size()));
                        System.out.println("当前缓存空间数据：" + concurrentHashMap);
                        for (String key : concurrentHashMap.keySet()) {
                            CacheEntity cacheEntity = concurrentHashMap.get(key);
                            if (localCache.checkIsExpire(cacheEntity)) {
                                concurrentHashMap.remove(key);
                                System.out.println("移除失效缓存：" + cacheEntity);
                            }
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /***
     * 缓存对象类
     */
    private static class CacheEntity implements Serializable {

        /***
         * 对应的键
         */
        private String key;
        /***
         * 缓存的值
         */
        private Object value;
        /***
         * 创建时间戳
         */
        private long timeStamp = System.currentTimeMillis();
        /***
         * 失效时间
         */
        private long expire = DEFAULT_TIMEOUT;

        String getKey() {
            return key;
        }

        Object getValue() {
            return value;
        }

        long getTimeStamp() {
            return timeStamp;
        }

        long getExpire() {
            return expire;
        }

        void setValue(Object value) {
            this.value = value;
        }

        void setTimeStamp(long timeStamp) {
            this.timeStamp = timeStamp;
        }

        void setExpire(long expire) {
            this.expire = expire;
        }

        CacheEntity(String key, Object value, long expire) {
            this.key = key;
            this.value = value;
            this.expire = expire;
        }

        CacheEntity(String key, Object value) {
            this.key = key;
            this.value = value;
        }

        @Override
        public String toString() {
            return "[" + key + "," + value + "," + timeStamp + "," + expire + ']';
        }
    }
}
