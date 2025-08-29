package com.example.webhookapp.model;

import jakarta.persistence.*;
import java.time.OffsetDateTime;

@Entity
@Table(name = "solutions")
public class Solution {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String regNo;
    private String questionId;

    @Lob
    private String finalQuery;

    private OffsetDateTime createdAt;

    public Solution() {}

    public Solution(String regNo, String questionId, String finalQuery) {
        this.regNo = regNo;
        this.questionId = questionId;
        this.finalQuery = finalQuery;
        this.createdAt = OffsetDateTime.now();
    }

    // getters & setters omitted for brevity (generate them or use Lombok)
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getRegNo() { return regNo; }
    public void setRegNo(String regNo) { this.regNo = regNo; }
    public String getQuestionId() { return questionId; }
    public void setQuestionId(String questionId) { this.questionId = questionId; }
    public String getFinalQuery() { return finalQuery; }
    public void setFinalQuery(String finalQuery) { this.finalQuery = finalQuery; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(OffsetDateTime createdAt) { this.createdAt = createdAt; }
}
