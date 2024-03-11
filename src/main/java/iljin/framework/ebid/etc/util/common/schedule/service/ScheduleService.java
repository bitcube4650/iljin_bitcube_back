package iljin.framework.ebid.etc.util.common.schedule.service;

import iljin.framework.ebid.etc.util.common.schedule.repository.ScheduleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ScheduleService {
    private final ScheduleRepository scheduleRepository;

    @Transactional
    public void deleteBidPlan() throws Exception {
        scheduleRepository.deleteByIngTag();
    }

    @Transactional
    public void updateIngTagForLast30Days() throws  Exception {
        scheduleRepository.updateIngTagForLast30Days();
    }
}
