/**
 * @Author: 霁
 * @Date: 2020/7/8 0:10
 */
public class Test {
    public static void main(String[] args) {
        LocalCache localCache = LocalCache.getInstance();
        localCache.set("1",1);
        localCache.set("2",2);
        localCache.set("3",3);
        localCache.set("4",4);
        localCache.set("5",5);
        System.out.println(LocalCache.getConcurrentHashMap());
        localCache.set("2","我是更新的2缓存");
        System.out.println(LocalCache.getConcurrentHashMap());
        System.out.println(localCache.get("1"));
        localCache.remove("1");
        localCache.set("6",6);
        localCache.set("7",7);
        localCache.set("8",8);
        localCache.set("9",9);
        localCache.set("10",10);
        localCache.set("11",11);
        localCache.set("12",12);
        localCache.set("13",13);
        localCache.set("14",14);
        localCache.set("15",15);
        System.out.println(LocalCache.getConcurrentHashMap());
    }
}
