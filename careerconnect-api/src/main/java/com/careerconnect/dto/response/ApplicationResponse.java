package com.careerconnect.dto.response;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ApplicationResponse {
    private Long id;
    private String jobTitle;
    private String companyName;
    private String applicantName;
    private LocalDateTime appliedAt;
}