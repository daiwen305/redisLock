package com.dev.lock.Controller;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.dev.lock.ServiceImpl.RedisLockImpl;

@RestController 
@RequestMapping("/distribution/redis") 
public class RedisLockController { 
 
  private static final String LOCK_NO = "redis_distribution_lock_no"; 
 
  private static int count = 0; 
 
  private ExecutorService service; 
 
  @Autowired 
  private StringRedisTemplate redisTemplate; 
 
  /** 
   * 模拟1000个线程同时执行业务，修改资源 
   * 
   * 使用线程池定义了20个线程 
   * 
   */ 
  @GetMapping("testLock") 
  public void testRedisDistributionLock(){ 
 
    service = Executors.newFixedThreadPool(20); 
 
    for (int i=0;i<5;i++){ 
      service.execute(new Runnable() { 
        @Override 
        public void run() { 
          task(Thread.currentThread().getName()); 
        } 
      }); 
    } 
 
  } 
 
  @GetMapping("/{key}") 
  public String getValue(@PathVariable("key") String key){ 
    String result = redisTemplate.opsForValue().get(key); 
    System.out.println(key+"==>"+result);
    return result; 
  } 
 
  private void task(String threadName) { 
 
    //创建一个redis分布式锁 
    RedisLockImpl redisLock = new RedisLockImpl(redisTemplate); 
    //加锁时间 
    Long lockTime = redisLock.lock(LOCK_NO, threadName); 
    if (lockTime!=null){ 
      //开始执行任务 
      System.out.println(threadName + "任务执行中"+(count++)); 
      //任务执行完毕 关闭锁 
      redisLock.unlock(LOCK_NO, lockTime, threadName); 
    } 
 
  } 
} 