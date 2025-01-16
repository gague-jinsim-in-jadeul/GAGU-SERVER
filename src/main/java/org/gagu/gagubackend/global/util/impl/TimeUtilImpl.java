package org.gagu.gagubackend.global.util.impl;

import org.gagu.gagubackend.global.util.TimeUtil;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

@Service
public class TimeUtilImpl implements TimeUtil {

    @Override
    public String makeTimeTemplate() {
        return ZonedDateTime.now(ZoneId.of("Asia/Seoul")).toLocalDateTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd a HH시 mm분 ss초"));
    }
}
