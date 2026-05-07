package com.careerconnect.service;

import com.careerconnect.dto.response.ApplicationResponse;
import com.careerconnect.entity.Application;
import com.careerconnect.entity.JobPost;
import com.careerconnect.entity.User;
import com.careerconnect.repository.ApplicationRepository;
import com.careerconnect.repository.JobPostRepository;
import com.careerconnect.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ApplicationService {

    @Autowired
    private ApplicationRepository applicationRepository;

    @Autowired
    private JobPostRepository jobPostRepository;

    @Autowired
    private UserRepository userRepository;

    public ApplicationResponse applyForJob(Long jobId, String seekerEmail) {
        // 1. Find the User (Seeker)
        User seeker = userRepository.findByEmail(seekerEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // 2. Find the Job Post
        JobPost job = jobPostRepository.findById(jobId)
                .orElseThrow(() -> new RuntimeException("Job not found"));

        // Prevent duplicate applications!
        if (applicationRepository.existsByApplicantIdAndJobPostId(seeker.getId(), jobId)) {
            throw new RuntimeException("You have already applied for this job!");
        }

        // 3. Create the Application
        Application application = new Application();
        application.setApplicant(seeker);
        application.setJobPost(job);
        application.setAppliedAt(LocalDateTime.now());
        // application.setStatus("PENDING"); // Uncomment if you have a status field!

        Application savedApplication = applicationRepository.save(application);

        // 4. Map to DTO to return
        ApplicationResponse response = new ApplicationResponse();
        response.setId(savedApplication.getId());
        response.setJobTitle(job.getTitle());
        response.setCompanyName(job.getCompanyName());
        response.setApplicantName(seeker.getFullName());
        response.setAppliedAt(savedApplication.getAppliedAt());

        return response;
    }

    // Add this below your applyForJob method
    public List<ApplicationResponse> getApplicationsForJob(Long jobId, String employerEmail) {

        // 1. Find the job
        JobPost job = jobPostRepository.findById(jobId)
                .orElseThrow(() -> new RuntimeException("Job not found"));

        // 2. RESOURCE OWNERSHIP CHECK: Does this job actually belong to the person requesting it?
        if (!job.getEmployer().getEmail().equals(employerEmail)) {
            throw new RuntimeException("Unauthorized: You can only view applications for your own job posts.");
        }

        // 3. Fetch the applications from the database
        List<Application> applications = applicationRepository.findByJobPostId(jobId);

        // 4. Convert the database entities into safe DTOs
        return applications.stream().map(app -> {
            ApplicationResponse dto = new ApplicationResponse();
            dto.setId(app.getId());
            dto.setJobTitle(job.getTitle());
            dto.setCompanyName(job.getCompanyName());
            dto.setApplicantName(app.getApplicant().getFullName());
            dto.setAppliedAt(app.getAppliedAt());
            return dto;
        }).collect(java.util.stream.Collectors.toList());
    }

    // GET MY APPLICATIONS (For Seekers)
    public List<ApplicationResponse> getMyApplications(String seekerEmail) {

        // 1. Find the user making the request
        User seeker = userRepository.findByEmail(seekerEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // 2. Fetch their applications from the database
        List<Application> applications = applicationRepository.findByApplicantId(seeker.getId());

        // 3. Convert to safe DTOs
        return applications.stream().map(app -> {
            ApplicationResponse dto = new ApplicationResponse();
            dto.setId(app.getId());
            dto.setJobTitle(app.getJobPost().getTitle()); // Note: Assumes Application entity has getJobPost()
            dto.setCompanyName(app.getJobPost().getCompanyName());
            dto.setApplicantName(seeker.getFullName());
            dto.setAppliedAt(app.getAppliedAt());
            return dto;
        }).collect(java.util.stream.Collectors.toList());
    }
}