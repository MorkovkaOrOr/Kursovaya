package com.hibernate;

import java.util.List;
import javax.persistence.*;

@Entity
@Table(name = "company")
public class Company {

    private String companyName;
    private int idcompany;
    private List<Contract> contracts;

    // Конструктор по умолчанию
    public Company() {
    }

    // Конструктор с параметром
    public Company(String companyName) {
        this.companyName = companyName;
    }

    // Геттер и сеттер для idcompany
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idcompany")
    public int getIdcompany() {
        return idcompany;
    }

    public void setIdcompany(int idcompany) {
        this.idcompany = idcompany;
    }

    // Геттер и сеттер для companyName
    @Column(name = "company_name")
    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    // Геттер и сеттер для contracts
    @OneToMany(mappedBy = "company", cascade = CascadeType.ALL)
    public List<Contract> getContracts() {
        return contracts;
    }

    public void setContracts(List<Contract> contracts) {
        this.contracts = contracts;
    }
}
