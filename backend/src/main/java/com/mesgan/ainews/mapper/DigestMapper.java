package com.mesgan.ainews.mapper;

import com.mesgan.ainews.dto.DigestResponse;
import com.mesgan.ainews.entity.DailyDigest;
import org.springframework.stereotype.Component;

@Component
public class DigestMapper {

    public DigestResponse toResponse(DailyDigest digest) {
        return new DigestResponse(
                digest.getId(),
                digest.getDigestDate(),
                digest.getContent(),
                digest.getCreatedAt()
        );
    }
}
