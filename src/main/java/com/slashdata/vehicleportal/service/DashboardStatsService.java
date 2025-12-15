package com.slashdata.vehicleportal.service;

import com.slashdata.vehicleportal.repository.ADPMappingRepository;
import com.slashdata.vehicleportal.repository.ADPMasterRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class DashboardStatsService {

    private static final Logger LOG = LoggerFactory.getLogger(DashboardStatsService.class);

    private final ADPMasterRepository adpMasterRepository;
    private final ADPMappingRepository adpMappingRepository;

    public DashboardStatsService(ADPMasterRepository adpMasterRepository,
                                 ADPMappingRepository adpMappingRepository) {
        this.adpMasterRepository = adpMasterRepository;
        this.adpMappingRepository = adpMappingRepository;
    }

    @Async
    public void recalculateDashboardAsync() {
        long masters = adpMasterRepository.count();
        long mappings = adpMappingRepository.count();
        LOG.debug("Recalculated dashboard stats - masters: {}, mappings: {}", masters, mappings);
    }
}
