package org.example.backend_pcbuild.Admin.service;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.backend_pcbuild.Admin.dto.OfferUpdateType;
import org.example.backend_pcbuild.Admin.repository.OfferUpdateConfigRepository;
import org.example.backend_pcbuild.models.OfferUpdateConfig;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.concurrent.ScheduledFuture;


@Service
@RequiredArgsConstructor
@Slf4j
public class OfferUpdateConfigService {

    private final OfferUpdateService offerUpdateService;
    private final OfferUpdateConfigRepository offerUpdateConfigRepository;
    private final ThreadPoolTaskScheduler updateScheduler = new ThreadPoolTaskScheduler();
    private ScheduledFuture<?> scheduledUpdate;



    @PostConstruct
    public void initScheduler() {
        updateScheduler.initialize();
    }


    public void updateUpdateInterval(String interval){
        Optional<OfferUpdateConfig> offerUpdateConfig = offerUpdateConfigRepository.findByType(OfferUpdateType.AUTOMATIC);
        if(offerUpdateConfig.isPresent()) {
            offerUpdateConfig.get().setIntervalTime(interval);
            offerUpdateConfigRepository.save(offerUpdateConfig.get());
        }
        if (interval == null || interval.isBlank()) return;
        if (interval.contains("h")) {
            restartScheduledUpdate("0 0 */" + interval.replace("h", "") + " * * ?" );
        } else if (interval.contains("min")) {
            restartScheduledUpdate("0 */" + interval.replace("min", "") + " * * * ?" );
        }
    }

    private void restartScheduledUpdate(String cronExpression){
        if (scheduledUpdate != null) {
            scheduledUpdate.cancel(false);
        }
        scheduledUpdate = updateScheduler.schedule(offerUpdateService.scheduledAutomaticOfferUpdate(), new CronTrigger(cronExpression));
    }


//    public OfferUpdateConfig saveOfferUpdateConfig(Integer intervalInMinutes, OfferUpdateType type){
//        Optional<OfferUpdateConfig> offerUpdateConfig = offerUpdateConfigRepository.findByType(type);
//        if(offerUpdateConfig.isPresent()) {
//            return null;
//        }
//        return offerUpdateConfigRepository.save(new OfferUpdateConfig(type, intervalInMinutes));
//    }
}
