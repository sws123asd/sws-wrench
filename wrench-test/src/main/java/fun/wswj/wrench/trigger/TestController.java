package fun.wswj.wrench.trigger;

import fun.wswj.wrench.dcc.domain.model.valobj.AttributeVO;
import fun.wswj.wrench.dcc.types.annotations.DCCValue;
import fun.wswj.wrench.domain.model.User;
import fun.wswj.wrench.idempotent.lock.types.annotations.IdempotentLock;
import org.redisson.api.RTopic;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import javax.annotation.Resource;


@RestController
@RequestMapping("/test")
public class TestController {

    @DCCValue("jiangji:close")
    private String jiangji;
    @Resource
    private RTopic dynamicConfigCenterRedisTopic;
//    @RateLimiterAccessInterceptor(key = "userId",permits = 2, seconds = 10, blacklistCount = 5, fallbackMethod = "drawErrorRateLimiter")
    @IdempotentLock(keys = {"#user.userId","#user.name"})
    @GetMapping("/hello")
    public String hello(User user) throws InterruptedException {
        if ("open".equals(jiangji)){
            return "服务降级";
        }
        Thread.sleep(10000);
        return "hello";

    }

    public String drawErrorRateLimiter(String userId) {
        return "rateLimiter";
    }

    @GetMapping("/updateDcc")
    public String update(String attribute, String value) {
        dynamicConfigCenterRedisTopic.publish(new AttributeVO(attribute, value));
        return "success";
    }

    private static final String BASE_URL = "http://localhost:9191/test/hello?userId=sws123asd&name=songwenshuai";

    public static void main(String[] args) {
        RestTemplate restTemplate = new RestTemplate();

        // 第一个线程：模拟第一个请求
        Thread thread1 = new Thread(() -> {
            System.out.println("[" + System.currentTimeMillis() + "] 发起第一次请求...");
            String response = restTemplate.getForObject(BASE_URL, String.class);
            System.out.println("[" + System.currentTimeMillis() + "] 第一次响应: " + response);
        });

        // 第二个线程：模拟第二次请求，在第一个请求开始后立即发起
        Thread thread2 = new Thread(() -> {
            try {
                Thread.sleep(1000); // 等待1秒，确保第一次请求已开始
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println("[" + System.currentTimeMillis() + "] 发起第二次请求...");
            String response = restTemplate.getForObject(BASE_URL, String.class);
            System.out.println("[" + System.currentTimeMillis() + "] 第二次响应: " + response);
        });

        // 启动两个线程
        thread1.start();
        thread2.start();
    }
}
