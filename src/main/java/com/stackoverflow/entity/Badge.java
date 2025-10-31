package com.stackoverflow.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "badges")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Badge {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false)
    private String type; // BRONZE, SILVER, GOLD

    @Column(nullable = false)
    private String category; // QUESTION, ANSWER, PARTICIPATION, REPUTATION, SPECIAL

    private String icon;

    @Column(nullable = false)
    private Integer requiredPoints = 0;

    private Integer requiredQuestions;

    private Integer requiredAnswers;

    private Integer requiredReputation;

    @Column(nullable = false)
    private Boolean isActive = true;

    @ManyToMany(mappedBy = "badges")
    private Set<User> users = new HashSet<>();

    @Column(nullable = false)
    private Integer earnedCount = 0;

    public void incrementEarnedCount() {
        this.earnedCount++;
    }

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getCategory() {
		return category;
	}

	public void setCategory(String category) {
		this.category = category;
	}

	public String getIcon() {
		return icon;
	}

	public void setIcon(String icon) {
		this.icon = icon;
	}

	public Integer getRequiredPoints() {
		return requiredPoints;
	}

	public void setRequiredPoints(Integer requiredPoints) {
		this.requiredPoints = requiredPoints;
	}

	public Integer getRequiredQuestions() {
		return requiredQuestions;
	}

	public void setRequiredQuestions(Integer requiredQuestions) {
		this.requiredQuestions = requiredQuestions;
	}

	public Integer getRequiredAnswers() {
		return requiredAnswers;
	}

	public void setRequiredAnswers(Integer requiredAnswers) {
		this.requiredAnswers = requiredAnswers;
	}

	public Integer getRequiredReputation() {
		return requiredReputation;
	}

	public void setRequiredReputation(Integer requiredReputation) {
		this.requiredReputation = requiredReputation;
	}

	public Boolean getIsActive() {
		return isActive;
	}

	public void setIsActive(Boolean isActive) {
		this.isActive = isActive;
	}

	public Set<User> getUsers() {
		return users;
	}

	public void setUsers(Set<User> users) {
		this.users = users;
	}

	public Integer getEarnedCount() {
		return earnedCount;
	}

	public void setEarnedCount(Integer earnedCount) {
		this.earnedCount = earnedCount;
	}
    
}

