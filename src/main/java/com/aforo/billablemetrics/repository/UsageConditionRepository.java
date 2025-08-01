package com.aforo.billablemetrics.repository;

import com.aforo.billablemetrics.entity.UsageCondition;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UsageConditionRepository extends JpaRepository<UsageCondition, Long> {
}
