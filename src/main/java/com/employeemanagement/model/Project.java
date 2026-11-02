package com.employeemanagement.model;

import jakarta.persistence.*;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "projectinfo")
public class Project {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "project_id")
    private long id;

    @Column(name = "project_name", nullable = true, length = 150)
    private String projectName;

    @Column(name = "client_name", nullable = true, length = 150)
    private String clientName;

    @Column(name = "start_date", nullable = true)
    private LocalDate startDate;

    @Column(name = "budget", nullable = true)
    private double budget;

    @Column(name = "maximum_team_size", nullable = true)
    private int maximumTeamSize;

    @Column(name = "status", nullable = true, length = 20)
    private String status;

    @ManyToMany
    @JoinTable(
        name = "employee_project",
        joinColumns = @JoinColumn(name = "project_id"),
        inverseJoinColumns = @JoinColumn(name = "emp_id")
    )
    private Set<Employee> employees = new HashSet<>();

    public Project() {
    }

    public Project(String projectName, String clientName, LocalDate startDate, double budget, int maximumTeamSize, String status) {
        this.projectName = projectName;
        this.clientName = clientName;
        this.startDate = startDate;
        this.budget = budget;
        this.maximumTeamSize = maximumTeamSize;
        this.status = status;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getProjectName() {
        return projectName;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    public String getClientName() {
        return clientName;
    }

    public void setClientName(String clientName) {
        this.clientName = clientName;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public double getBudget() {
        return budget;
    }

    public void setBudget(double budget) {
        this.budget = budget;
    }

    public int getMaximumTeamSize() {
        return maximumTeamSize;
    }

    public void setMaximumTeamSize(int maximumTeamSize) {
        this.maximumTeamSize = maximumTeamSize;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Set<Employee> getEmployees() {
        return employees;
    }

    public void setEmployees(Set<Employee> employees) {
        this.employees = employees;
    }
}
