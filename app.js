const API_BASE_URL = "http://localhost:8080/api";
let currentLoginRole = "SEEKER"; // Default tab

// --- UI & STATE MANAGEMENT ---

function showAlert(message, isSuccess) {
    const box = document.getElementById("alert-box");
    box.innerText = message;
    box.className = isSuccess ? "alert-success" : "alert-error";
    box.classList.remove("hidden");
    setTimeout(() => box.classList.add("hidden"), 4000);
}

function switchLoginRole(role) {
    currentLoginRole = role;
    document.getElementById("tab-seeker").classList.toggle("active", role === "SEEKER");
    document.getElementById("tab-employer").classList.toggle("active", role === "EMPLOYER");
    
    // Auto-fill emails for testing convenience
    const emailInput = document.getElementById("login-email");
    emailInput.value = role === "SEEKER" ? "subash@example.com" : "hr@techcorp.com";
}

function getAuthHeaders() {
    return {
        "Content-Type": "application/json",
        "Authorization": `Bearer ${localStorage.getItem("jwtToken")}`
    };
}

// --- AUTHENTICATION ---

async function login() {
    const email = document.getElementById("login-email").value;
    const password = document.getElementById("login-password").value;

    try {
        const response = await fetch(`${API_BASE_URL}/auth/login`, {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({ email, password })
        });

        if (response.ok) {
            const data = await response.json();
            localStorage.setItem("jwtToken", data.token);
            localStorage.setItem("userRole", currentLoginRole); // Save the role!
            
            document.getElementById("view-login").classList.add("hidden");
            document.getElementById("navbar").classList.remove("hidden");
            document.getElementById("user-role-badge").innerText = currentLoginRole;

            // Route to correct dashboard
            if(currentLoginRole === "SEEKER") {
                document.getElementById("view-seeker").classList.remove("hidden");
                loadPublicJobs(); // Auto-load jobs for seekers
            } else {
                document.getElementById("view-employer").classList.remove("hidden");
            }
            showAlert("Login successful!", true);
        } else {
            showAlert("Invalid credentials.", false);
        }
    } catch (error) {
        showAlert("Server offline. Is Spring Boot running?", false);
    }
}

function logout() {
    localStorage.removeItem("jwtToken");
    localStorage.removeItem("userRole");
    window.location.reload(); // Refresh the page to reset state
}

// --- SEEKER FUNCTIONS ---

async function loadPublicJobs() {
    try {
        const response = await fetch(`${API_BASE_URL}/jobs/public/all`);
        if (response.ok) {
            const jobs = await response.json();
            const container = document.getElementById("public-jobs-container");
            container.innerHTML = "";
            
            jobs.forEach(job => {
                container.innerHTML += `
                    <div class="job-card">
                        <div class="job-title">${job.title} <span style="font-size: 0.8rem; color: #9ca3af;">(ID: ${job.id})</span></div>
                        <div class="job-company">🏢 ${job.companyName}</div>
                        <div class="job-details">
                            📍 ${job.location} <br>
                            💰 ${job.salaryRange} <br>
                            📝 ${job.description}
                        </div>
                        <button class="apply-btn" onclick="applyForJob(${job.id})">Apply Now</button>
                    </div>
                `;
            });
        }
    } catch (error) {
        console.error(error);
    }
}

async function applyForJob(jobId) {
    try {
        const response = await fetch(`${API_BASE_URL}/applications/${jobId}/apply`, {
            method: "POST",
            headers: getAuthHeaders()
        });
        
        if (response.ok) {
            showAlert("Application submitted successfully!", true);
            loadMyApplications(); // Auto-refresh their list
        } else {
            const errorData = await response.json().catch(() => null);
            showAlert(errorData ? errorData.message : "Failed to apply. Already applied?", false);
        }
    } catch (error) {
        console.error(error);
    }
}

async function loadMyApplications() {
    try {
        const response = await fetch(`${API_BASE_URL}/applications/my-applications`, {
            method: "GET", headers: getAuthHeaders()
        });
        if (response.ok) {
            const apps = await response.json();
            const container = document.getElementById("my-applications-container");
            container.innerHTML = apps.length === 0 ? "<p>No applications found.</p>" : "";
            
            apps.forEach(app => {
                container.innerHTML += `
                    <div class="job-card" style="border-left: 4px solid var(--secondary);">
                        <div class="job-title">${app.jobTitle}</div>
                        <div class="job-company">${app.companyName}</div>
                        <div class="job-details">📅 Applied on: ${new Date(app.appliedAt).toLocaleDateString()}</div>
                    </div>
                `;
            });
        }
    } catch (error) { console.error(error); }
}

// --- EMPLOYER FUNCTIONS ---

async function postJob() {
    const jobData = {
        title: document.getElementById("job-title").value,
        companyName: document.getElementById("job-company").value,
        location: document.getElementById("job-location").value,
        salaryRange: document.getElementById("job-salary").value,
        description: document.getElementById("job-desc").value
    };

    try {
        const response = await fetch(`${API_BASE_URL}/jobs/create`, {
            method: "POST", headers: getAuthHeaders(), body: JSON.stringify(jobData)
        });
        
        if (response.ok) {
            showAlert("Job posted successfully!", true);
            // Clear form
            document.querySelectorAll("#view-employer input, #view-employer textarea").forEach(el => el.value = "");
        } else {
            showAlert("Failed to post job.", false);
        }
    } catch (error) { console.error(error); }
}

async function checkApplicants() {
    const jobId = document.getElementById("check-job-id").value;
    if(!jobId) return showAlert("Please enter a Job ID", false);

    try {
        const response = await fetch(`${API_BASE_URL}/applications/job/${jobId}`, {
            method: "GET", headers: getAuthHeaders()
        });
        
        const container = document.getElementById("applicants-container");
        if (response.ok) {
            const applicants = await response.json();
            container.innerHTML = applicants.length === 0 ? "<p>No applicants yet.</p>" : "";
            
            applicants.forEach(app => {
                container.innerHTML += `
                    <div style="padding: 1rem; border: 1px solid var(--border); border-radius: 8px; margin-bottom: 0.5rem; display: flex; justify-content: space-between;">
                        <div><strong>${app.applicantName}</strong> applied for ${app.jobTitle}</div>
                        <div style="color: var(--text-muted); font-size: 0.875rem;">${new Date(app.appliedAt).toLocaleDateString()}</div>
                    </div>
                `;
            });
        } else {
            showAlert("Unauthorized or Job not found. Make sure this is YOUR job post.", false);
            container.innerHTML = "";
        }
    } catch (error) { console.error(error); }
}

// Initialize: Check if already logged in on page reload
window.onload = () => switchLoginRole('SEEKER');