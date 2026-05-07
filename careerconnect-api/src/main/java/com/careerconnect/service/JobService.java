package com.careerconnect.service;

import com.careerconnect.dto.request.JobPostRequest;
import com.careerconnect.dto.response.JobDetailResponse;
import com.careerconnect.entity.JobPost;
import com.careerconnect.entity.User;
import com.careerconnect.repository.JobPostRepository;
import com.careerconnect.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class JobService {

    @Autowired
    private JobPostRepository jobPostRepository;

    @Autowired
    private UserRepository userRepository;

    // 1. Create a new Job Post (Only Employers can do this)
    public JobDetailResponse createJobPost(JobPostRequest request, String employerEmail) {

        // Find the employer in the database using the email from their JWT token
        User employer = userRepository.findByEmail(employerEmail)
                .orElseThrow(() -> new RuntimeException("Employer not found!"));

        // Convert the DTO into a real Database Entity
        JobPost jobPost = new JobPost();
        jobPost.setTitle(request.getTitle());
        jobPost.setDescription(request.getDescription());
        jobPost.setCompanyName(request.getCompanyName());
        jobPost.setLocation(request.getLocation());
        jobPost.setSalaryRange(request.getSalaryRange());
        jobPost.setEmployer(employer); // Link the job to the user!

        // Save to MySQL
        JobPost savedJob = jobPostRepository.save(jobPost);

        // Return the saved data back to the frontend
        return mapToDto(savedJob);
    }

    // 2. Get all jobs (Publicly available)
    public List<JobDetailResponse> getAllJobs() {
        List<JobPost> jobs = jobPostRepository.findAll();

        // Convert the list of Database Entities into a list of safe DTOs
        return jobs.stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    // Helper method to convert an Entity to a DTO
    private JobDetailResponse mapToDto(JobPost jobPost) {
        JobDetailResponse dto = new JobDetailResponse();
        dto.setId(jobPost.getId());
        dto.setTitle(jobPost.getTitle());
        dto.setDescription(jobPost.getDescription());
        dto.setCompanyName(jobPost.getCompanyName());
        dto.setLocation(jobPost.getLocation());
        dto.setSalaryRange(jobPost.getSalaryRange());
        dto.setPostedAt(jobPost.getPostedAt());
        dto.setEmployerName(jobPost.getEmployer().getFullName());
        return dto;
    }
}