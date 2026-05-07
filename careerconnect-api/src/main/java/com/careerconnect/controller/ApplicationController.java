package com.careerconnect.controller;

import com.careerconnect.dto.response.ApplicationResponse;
import com.careerconnect.service.ApplicationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/applications")
@CrossOrigin("*")
public class ApplicationController {

    @Autowired
    private ApplicationService applicationService;

    // POST /api/applications/{jobId}/apply
    @PostMapping("/{jobId}/apply")
    public ResponseEntity<ApplicationResponse> apply(@PathVariable Long jobId) {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        // Ensure only Seekers can apply
        boolean isSeeker = authentication.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_SEEKER"));

        if (!isSeeker) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN); // Block Employers from applying to jobs!
        }

        String seekerEmail = authentication.getName();

        ApplicationResponse response = applicationService.applyForJob(jobId, seekerEmail);

        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    // Add this below your existing @PostMapping

    // GET /api/applications/job/{jobId}
    @GetMapping("/job/{jobId}")

    public ResponseEntity<List<ApplicationResponse>> getJobApplications(@PathVariable Long jobId) {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        // 1. Ensure only Employers can hit this endpoint
        boolean isEmployer = authentication.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_EMPLOYER"));

        if (!isEmployer) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN); // Seekers shouldn't see all applicants!
        }

        // 2. Grab the employer's email from the token
        String employerEmail = authentication.getName();

        // 3. Fetch the data
        List<ApplicationResponse> responses = applicationService.getApplicationsForJob(jobId, employerEmail);

        return ResponseEntity.ok(responses);
    }

    // GET /api/applications/my-applications
    @GetMapping("/my-applications")
    public ResponseEntity<List<ApplicationResponse>> getMyApplications() {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        // 1. Ensure only Seekers can hit this endpoint
        boolean isSeeker = authentication.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_SEEKER"));

        if (!isSeeker) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }

        // 2. Grab the seeker's email from the token
        String seekerEmail = authentication.getName();

        // 3. Fetch the data
        List<ApplicationResponse> responses = applicationService.getMyApplications(seekerEmail);

        return ResponseEntity.ok(responses);
    }
}