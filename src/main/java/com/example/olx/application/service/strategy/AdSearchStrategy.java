// com/example/olx/application/service/strategy/AdSearchStrategy.java (для Strategy)
package com.example.olx.application.service.strategy;

import com.example.olx.domain.model.Ad;
import java.util.List;
import java.util.Map;

public interface AdSearchStrategy {
    List<Ad> search(List<Ad> allAds, Map<String, Object> criteria);
}