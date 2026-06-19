package com.mesgan.ainews.repository;

import com.mesgan.ainews.entity.DailyDigest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface DailyDigestRepository extends JpaRepository<DailyDigest, UUID> {

    Optional<DailyDigest> findByDigestDate(LocalDate date);
}
