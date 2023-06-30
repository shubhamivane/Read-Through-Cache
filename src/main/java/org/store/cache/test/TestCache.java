package org.store.cache.test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.store.cache.Cache;
import org.store.cache.redis.RedisCache;

import javax.annotation.PostConstruct;
import java.util.function.Function;

@Configuration
public class TestCache {

  @Autowired
  private StringRedisTemplate stringRedisTemplate;

  private Function<String, User> reloader = key -> {
    System.out.println("Reloading");
    User user = new User();
    user.setLastName("ivane");
    user.setName("shubham");
    return null;
  };


  @PostConstruct
  public void init() throws InterruptedException {
    System.out.println("\n Starting Testing");
    Cache<String, User> cache = new RedisCache.Builder<String, User>().setRedisTemplate(stringRedisTemplate).setName("user").setEntityClass(User.class).setTtl(3L).setReloader(reloader).build();
    System.out.println(cache.get("shubham"));
    Thread.sleep(10000);
    System.out.println(cache.get("shubham"));
  }
}
