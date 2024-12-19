package com.hibernate;

import java.util.List;
import javax.persistence.*;

@Entity
@Table(name = "company")
public class Company {

    private String companyName;
    private int idcompany;
    private List<Contract> contracts;

    // ��������� �� 㬮�砭��
    public Company() {
    }

    // ��������� � ��ࠬ��஬
    public Company(String companyName) {
        this.companyName = companyName;
    }

    // ����� � ���� ��� idcompany
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idcompany")
    public int getIdcompany() {
        return idcompany;
    }

    public void setIdcompany(int idcompany) {
        this.idcompany = idcompany;
    }

    // ����� � ���� ��� companyName
    @Column(name = "company_name")
    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    // ����� � ���� ��� contracts
    @OneToMany(mappedBy = "company", cascade = CascadeType.ALL)
    public List<Contract> getContracts() {
        return contracts;
    }

    public void setContracts(List<Contract> contracts) {
        this.contracts = contracts;
    }
}
