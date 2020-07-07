import java.io.Serializable;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;


/**
 * @Author: 霁
 * @Date: 2020/7/7 20:32
 */
public class LocalCache implements Serializable {
    // 默认缓存过期时间
    private static final long DEFAULT_TIMEOUT = 36 * 1000;
    // 定时任务执行间隔
    private static final long TASK_TIME = 10;
    // 缓存空间大小
    private static final int DEFAULT_SIZE = 8;

    // 考虑高并发情况下数据安全使用ConcurrentHashMap
    private static final ConcurrentHashMap<String, CacheEntity> concurrentHashMap = new ConcurrentHashMap<>(DEFAULT_SIZE);

    static ConcurrentHashMap<String, CacheEntity> getConcurrentHashMap() {
        return concurrentHashMap;
    }

    private LocalCache() {
    }

    private static LocalCache localCache = null;

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
    public void set(String key, Object value, long expire) {
        // 已经存在则更新
        if (concurrentHashMap.containsKey(key)) {
            CacheEntity cacheEntity = concurrentHashMap.get(key);
            cacheEntity.setExpire(expire);
            cacheEntity.setTimeStamp(System.currentTimeMillis());
            cacheEntity.setValue(value);
            return;
        }
        if (localCache.checkIsFull()) {
            // FIFO
            List<CacheEntity> cacheEntities = new ArrayList<>();
            for (String k : concurrentHashMap.keySet()) {
                CacheEntity cacheEntity = concurrentHashMap.get(k);
                cacheEntities.add(cacheEntity);
            }
            System.out.println("排序前：" + cacheEntities);
            cacheEntities.sort(new Comparator<CacheEntity>() {
                @Override
                public int compare(CacheEntity o1, CacheEntity o2) {
                    return Long.compare(o1.getTimeStamp(), o2.getTimeStamp());
                }
            });
            System.out.println("排序后：" + cacheEntities);
            System.out.println("移除最先进入的缓存：" + cacheEntities.get(0).getKey());
            concurrentHashMap.remove(cacheEntities.get(0).getKey());
        }
        concurrentHashMap.put(key, new CacheEntity(key, value, expire));
    }

    /***
     * 设置缓存
     * @param key 键
     * @param value 值
     */
    public void set(String key, Object value) {
        // 已经存在则更新
        if (concurrentHashMap.containsKey(key)) {
            CacheEntity cacheEntity = concurrentHashMap.get(key);
            cacheEntity.setTimeStamp(System.currentTimeMillis());
            cacheEntity.setValue(value);
            System.out.println("更新缓存：" + cacheEntity);
            return;
        }
        if (localCache.checkIsFull()) {
            // FIFO
            List<CacheEntity> cacheEntities = new ArrayList<>();
            for (String k : concurrentHashMap.keySet()) {
                CacheEntity cacheEntity = concurrentHashMap.get(k);
                cacheEntities.add(cacheEntity);
            }
            System.out.println("排序前：" + cacheEntities);
            cacheEntities.sort(new Comparator<CacheEntity>() {
                @Override
                public int compare(CacheEntity o1, CacheEntity o2) {
                    return Long.compare(o1.getTimeStamp(), o2.getTimeStamp());
                }
            });
            System.out.println("排序后：" + cacheEntities);
            System.out.println("移除最先进入的缓存：" + cacheEntities.get(0).getKey());
            concurrentHashMap.remove(cacheEntities.get(0).getKey());
        }
        concurrentHashMap.put(key, new CacheEntity(key, value));
    }

    /***
     * 获取缓存
     * @param key 键
     * @return 返回缓存的值
     */
    public Object get(String key) {
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
    public void remove(String key) {
        concurrentHashMap.remove(key);
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
        System.out.println("检查失效缓存：");
        return (System.currentTimeMillis() - cacheEntity.getTimeStamp()) >= cacheEntity.getExpire();
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
                    TimeUnit.SECONDS.sleep(TASK_TIME);
                    for (String key : concurrentHashMap.keySet()) {
                        CacheEntity cacheEntity = concurrentHashMap.get(key);
                        if (localCache.checkIsExpire(cacheEntity)) {
                            concurrentHashMap.remove(key);
                            System.out.println("移除失效缓存：" + cacheEntity);
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
    private static class CacheEntity {

        private String key;
        private Object value;
        private long timeStamp = System.currentTimeMillis();
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
