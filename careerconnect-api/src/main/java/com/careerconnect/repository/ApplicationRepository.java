package com.careerconnect.repository;

import com.careerconnect.entity.Application;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ApplicationRepository extends JpaRepository<Application, Long> {
    // Allows a seeker to see all jobs they applied to
    List<Application> findByApplicantId(Long applicantId);

    // Allows an employer to see all applications for a specific job post
    List<Application> findByJobPostId(Long jobPostId);

    // Prevents a seeker from applying to the same job twice
    boolean existsByApplicantIdAndJobPostId(Long applicantId, Long jobPostId);
}