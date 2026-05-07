package com.careerconnect.dto.request;

import lombok.Data;

@Data
public class JobPostRequest {
    private String title;
    private String description;
    private String companyName;
    private String location;
    private String salaryRange;
}