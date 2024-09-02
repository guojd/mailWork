package com.pt.myworkcenter.prometheus.interceptor;

import com.pt.myworkcenter.util.SpringUtil;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Meter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tags;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.ToDoubleFunction;

public class CustomInterceptor implements HandlerInterceptor {
    private static final String CUSTOM_KPI_NAME_TIMER = "custom.kpi.timer"; //耗时
    private static final String CUSTOM_KPI_NAME_COUNTER = "custom.kpi.counter"; //api调用次数。
    private static final String CUSTOM_KPI_NAME_SUMMARY = "custom.kpi.summary"; //汇总率
    private static MeterRegistry registry;
    private long startTime;
    private GaugeNumber gaugeNumber = new GaugeNumber();

    void getRegistry(){
        if(registry == null){
            //这里使用的时SpringUtil获取Bean，没有用@Autowired注解，Autowired会因为加载时机问题导致拿不到；SpringUtil.getBean网上实现有很多，可以自行搜索；
            registry = SpringUtil.getBean(MeterRegistry.class);
        }
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        getRegistry();
        //记录接口开始调用的时间
        startTime = System.currentTimeMillis();
        return HandlerInterceptor.super.preHandle(request, response, handler);
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        HandlerInterceptor.super.postHandle(request, response, handler, modelAndView);
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        //统计调用次数
        registry.counter(CUSTOM_KPI_NAME_COUNTER,"uri", request.getRequestURI(), "method", request.getMethod(),
                "status", response.getStatus() + "", "exception", ex == null ? "" : ex.getMessage(), "outcome", response.getStatus() == 200 ? "SUCCESS" : "CLIENT_ERROR").increment();
        //统计单次耗时
        registry.timer(CUSTOM_KPI_NAME_TIMER,"uri", request.getRequestURI(), "method", request.getMethod(),
                "status", response.getStatus() + "", "exception", ex == null ? "" : ex.getMessage(), "outcome", response.getStatus() == 200 ? "SUCCESS" : "CLIENT_ERROR").record(System.currentTimeMillis() - startTime, TimeUnit.MILLISECONDS);
        //统计调用成功率，根据过滤Counter对象，获取计数
        Collection<Meter> meters = registry.get(CUSTOM_KPI_NAME_COUNTER).tag("uri", request.getRequestURI()).tag("method", request.getMethod()).meters();
        double total = 0;
        double success = 0;
        for (Meter meter : meters) {
            Counter counter = (Counter) meter;
            total += counter.count();
            String status = meter.getId().getTag("status");
            if (status.equals("200")){
                success+= counter.count();
            }
        }
        //保存对应的成功率到Map中
        String key = request.getMethod() + request.getRequestURI();
        gaugeNumber.setPercent(key, Double.valueOf(success / total * 100L));
        registry.gauge(CUSTOM_KPI_NAME_SUMMARY, Tags.of("uri", request.getRequestURI(), "method", request.getMethod()), gaugeNumber, new ToDoubleFunction<GaugeNumber>() {
            @Override
            public double applyAsDouble(GaugeNumber value) {
                return value.getPercent(key);
            }
        });
        HandlerInterceptor.super.afterCompletion(request, response, handler, ex);
    }
    // gauge监控某个对象，所以用内部类替代，然后根据tag标签区分对应的成功率；key 为 method + uri
    class GaugeNumber {
        Map<String,Double> map = new HashMap<>();

        public Double getPercent(String key) {
            return map.get(key);
        }

        public void setPercent(String key, Double percent) {
            map.put(key, percent);
        }
    }
}
