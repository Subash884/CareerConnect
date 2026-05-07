package com.careerconnect.dto.response;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class JobDetailResponse {
    private Long id;
    private String title;
    private String description;
    private String companyName;
    private String location;
    private String salaryRange;
    private LocalDateTime postedAt;
    private String employerName; // We will extract this from the User linked to the job
}