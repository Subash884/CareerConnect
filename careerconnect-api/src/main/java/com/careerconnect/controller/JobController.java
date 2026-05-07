package com.careerconnect.controller;

import com.careerconnect.dto.request.JobPostRequest;
import com.careerconnect.dto.response.JobDetailResponse;
import com.careerconnect.service.JobService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/jobs")
@CrossOrigin("*")
public class JobController {

    @Autowired
    private JobService jobService;

    // ==========================================
    // 1. PUBLIC ENDPOINT: View all jobs (No token needed)
    // ==========================================
    @GetMapping("/public/all")
    public ResponseEntity<List<JobDetailResponse>> getAllJobs() {
        return ResponseEntity.ok(jobService.getAllJobs());
    }

    // ==========================================
    // 2. PROTECTED ENDPOINT: Create a job (Requires Employer Token)
    // ==========================================
    @PostMapping("/create")
    public ResponseEntity<JobDetailResponse> createJobPost(@RequestBody JobPostRequest jobPostRequest) {

        // 1. Who is making this request? Grab their token details from Spring Security!
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String employerEmail = authentication.getName(); // This pulls the email we saved in the JWT

        // 2. Make sure they actually have the Employer role
        boolean isEmployer = authentication.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_EMPLOYER"));

        if (!isEmployer) {
            // If a SEEKER tries to post a job, kick them out!
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }

        // 3. Pass the data to the Service layer
        JobDetailResponse response = jobService.createJobPost(jobPostRequest, employerEmail);

        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }
}