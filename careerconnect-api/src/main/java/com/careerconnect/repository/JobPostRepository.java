package com.careerconnect.repository;

import com.careerconnect.entity.JobPost;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface JobPostRepository extends JpaRepository<JobPost, Long> {
    // Allows an employer to easily find all the jobs they have posted
    List<JobPost> findByEmployerId(Long employerId);
}