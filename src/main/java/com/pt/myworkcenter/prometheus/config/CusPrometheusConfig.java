package com.pt.myworkcenter.prometheus.config;

import io.micrometer.core.instrument.Meter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.config.NamingConvention;
import org.springframework.boot.actuate.autoconfigure.metrics.MeterRegistryCustomizer;
import org.springframework.context.annotation.Bean;

import java.util.Arrays;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Prometheus配置文件
 */
//@Configuration
public class CusPrometheusConfig {
    /**
     * 可用于替换 Prometheus中 公共的 http.server.requests度量名替换
     * @return
     */
    @Bean
    MeterRegistryCustomizer<MeterRegistry> metricsConfig() {
        return registry -> registry.config().namingConvention(new NamingConvention() {
            @Override
            public String name(String name, Meter.Type type, String baseUnit) {
                System.out.println("Prometheus meter type:"+type+" name:"+name);
                return (String)Arrays.stream(name.split("\\.")).filter(Objects::nonNull).collect(Collectors.joining("_"));
            }
        });
    }
}
