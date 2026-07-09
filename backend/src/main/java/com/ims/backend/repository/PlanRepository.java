package com.ims.backend.repository;

import com.ims.backend.entity.Plan;
import com.ims.backend.entity.enums.InsuranceType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PlanRepository extends JpaRepository<Plan, Long> {

    @Query("""
            SELECT p FROM Plan p
            WHERE (:activeOnly = false OR p.active = true)
              AND (:type IS NULL OR p.insuranceType = :type)
            """)
    Page<Plan> browse(@Param("activeOnly") boolean activeOnly,
                      @Param("type") InsuranceType type,
                      Pageable pageable);
}
