package com.employeemanagement.model;

import jakarta.persistence.*;

import java.util.HashSet;
import java.util.Set;

/**
 * Hibernate entity representing an employee account and its project memberships.
 */
@Entity
@Table(name="employeeinfo")
public class Employee {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="emp_id")

    private long id;
    @Column(name="emp_name",nullable=true,length=100)
    private String name;
    @Column(name="emp_dept",nullable=true,length=100)
    private String dept;
    @Column(name="emp_salary",nullable=true)
    private double salary;
    @Column(name="emp_email",nullable=true,length=150)
    private String email;
    @Column(name="emp_designation",nullable=true,length=100)
    private String designation;
    @Column(name="emp_password",nullable=true,length=255)
    private String password;
    @Column(name="emp_role",nullable=true,length=20)
    private String role;

    // Project owns the join table; this is the inverse side of the association.
    @ManyToMany(mappedBy = "employees")
    private Set<Project> projects = new HashSet<>();

    public Employee(){

    }
    public Employee(String dept, double salary, String name) {
        this.dept = dept;
        this.salary = salary;
        this.name = name;
    }
    public Employee(String dept, double salary, String name, String email, String designation, String password) {
        this.dept = dept;
        this.salary = salary;
        this.name = name;
        this.email = email;
        this.designation = designation;
        this.password = password;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDept() {
        return dept;
    }

    public void setDept(String dept) {
        this.dept = dept;
    }

    public double getSalary() {
        return salary;
    }

    public void setSalary(double salary) {
        this.salary = salary;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getDesignation() {
        return designation;
    }

    public void setDesignation(String designation) {
        this.designation = designation;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public Set<Project> getProjects() {
        return projects;
    }

    public void setProjects(Set<Project> projects) {
        this.projects = projects;
    }
}
