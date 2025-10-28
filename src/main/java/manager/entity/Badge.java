package manager.entity;

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
}

