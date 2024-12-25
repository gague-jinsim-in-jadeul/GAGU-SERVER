package org.gagu.gagubackend.global.service.impl;

import org.gagu.gagubackend.global.service.TimeService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
public class TimeServiceImpl implements TimeService {
    @Override
    public String makeTimeTemplate() {
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd a HH시 mm분 ss초"));
    }
}
