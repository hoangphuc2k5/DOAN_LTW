package com.stackoverflow.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "system_settings")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class SystemSettings {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String siteName = "StackOverflow Clone";

    @Column(columnDefinition = "TEXT")
    private String siteDescription = "A Q&A platform for developers";

    @Column(nullable = false)
    private String contactEmail = "admin@stackoverflow.com";

    @Column(nullable = false)
    private String language = "en";

    @Column(nullable = false)
    private String theme = "light";

    @Column(nullable = false)
    private Boolean enableRegistration = true;

    @Column(nullable = false)
    private Boolean enableComments = true;

    @Column(nullable = false)
    private Boolean requireEmailVerification = false;

    @Column(nullable = false)
    private Boolean requireModeration = false;

    @Column(nullable = false)
    private Long maxUploadSize = 10L; // MB

    @Column(nullable = false)
    private Integer pointsPerQuestion = 10;

    @Column(nullable = false)
    private Integer pointsPerAnswer = 15;

    @Column(nullable = false)
    private Integer pointsPerAcceptedAnswer = 25;

    // Logo and Branding
    private String logoPath;
    
    private String faviconPath;

    // Theme Colors (allow null for existing databases)
    @Column(columnDefinition = "VARCHAR(255) DEFAULT '#0066cc'")
    private String primaryColor = "#0066cc";

    @Column(columnDefinition = "VARCHAR(255) DEFAULT '#28a745'")
    private String secondaryColor = "#28a745";

    @Column(columnDefinition = "VARCHAR(255) DEFAULT '#ffc107'")
    private String accentColor = "#ffc107";

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "updated_by")
    private User updatedBy;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getSiteName() {
		return siteName;
	}

	public void setSiteName(String siteName) {
		this.siteName = siteName;
	}

	public String getSiteDescription() {
		return siteDescription;
	}

	public void setSiteDescription(String siteDescription) {
		this.siteDescription = siteDescription;
	}

	public String getContactEmail() {
		return contactEmail;
	}

	public void setContactEmail(String contactEmail) {
		this.contactEmail = contactEmail;
	}

	public String getLanguage() {
		return language;
	}

	public void setLanguage(String language) {
		this.language = language;
	}

	public String getTheme() {
		return theme;
	}

	public void setTheme(String theme) {
		this.theme = theme;
	}

	public Boolean getEnableRegistration() {
		return enableRegistration;
	}

	public void setEnableRegistration(Boolean enableRegistration) {
		this.enableRegistration = enableRegistration;
	}

	public Boolean getEnableComments() {
		return enableComments;
	}

	public void setEnableComments(Boolean enableComments) {
		this.enableComments = enableComments;
	}

	public Boolean getRequireEmailVerification() {
		return requireEmailVerification;
	}

	public void setRequireEmailVerification(Boolean requireEmailVerification) {
		this.requireEmailVerification = requireEmailVerification;
	}

	public Boolean getRequireModeration() {
		return requireModeration;
	}

	public void setRequireModeration(Boolean requireModeration) {
		this.requireModeration = requireModeration;
	}

	public Long getMaxUploadSize() {
		return maxUploadSize;
	}

	public void setMaxUploadSize(Long maxUploadSize) {
		this.maxUploadSize = maxUploadSize;
	}

	public Integer getPointsPerQuestion() {
		return pointsPerQuestion;
	}

	public void setPointsPerQuestion(Integer pointsPerQuestion) {
		this.pointsPerQuestion = pointsPerQuestion;
	}

	public Integer getPointsPerAnswer() {
		return pointsPerAnswer;
	}

	public void setPointsPerAnswer(Integer pointsPerAnswer) {
		this.pointsPerAnswer = pointsPerAnswer;
	}

	public Integer getPointsPerAcceptedAnswer() {
		return pointsPerAcceptedAnswer;
	}

	public void setPointsPerAcceptedAnswer(Integer pointsPerAcceptedAnswer) {
		this.pointsPerAcceptedAnswer = pointsPerAcceptedAnswer;
	}

	public String getLogoPath() {
		return logoPath;
	}

	public void setLogoPath(String logoPath) {
		this.logoPath = logoPath;
	}

	public String getFaviconPath() {
		return faviconPath;
	}

	public void setFaviconPath(String faviconPath) {
		this.faviconPath = faviconPath;
	}

	public String getPrimaryColor() {
		return primaryColor;
	}

	public void setPrimaryColor(String primaryColor) {
		this.primaryColor = primaryColor;
	}

	public String getSecondaryColor() {
		return secondaryColor;
	}

	public void setSecondaryColor(String secondaryColor) {
		this.secondaryColor = secondaryColor;
	}

	public String getAccentColor() {
		return accentColor;
	}

	public void setAccentColor(String accentColor) {
		this.accentColor = accentColor;
	}

	public LocalDateTime getUpdatedAt() {
		return updatedAt;
	}

	public void setUpdatedAt(LocalDateTime updatedAt) {
		this.updatedAt = updatedAt;
	}

	public User getUpdatedBy() {
		return updatedBy;
	}

	public void setUpdatedBy(User updatedBy) {
		this.updatedBy = updatedBy;
	}
}

