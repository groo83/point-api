package com.groo83.point.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "point")
public class PointPolicyProperties {

    private Long oneTimeMaximumLimit;
}