package com.hibernate;

import java.time.LocalDate;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;

@Entity
@Table(name = "contract")
class Contract {

    @Id
    @Column(name = "idcontract")
    @GeneratedValue(strategy=  GenerationType.IDENTITY)
    private int contractId;

    @Column(name = "contract_end")
    private LocalDate contractEnd;

    @ManyToOne
    @JoinColumn(name = "IDcompany", referencedColumnName = "idcompany")
    private Company company;

    // Связь "один ко многим" с Cargo
    @OneToMany(mappedBy = "contract", cascade = CascadeType.ALL)
    private List<Cargo> cargos;

    // Конструктор без параметров
    public Contract() {}

    // Конструктор с параметрами
    public Contract(LocalDate end, Company company) {
        this.contractEnd = end;
        this.company = company;
    }

    // Геттеры и сеттеры
    public int getContractId() {
        return contractId;
    }

    public void setContractId(int contractId) {
        this.contractId = contractId;
    }

    public LocalDate getContractDate() {
        return contractEnd;
    }

    public void setContractDate(LocalDate contractEnd) {
        this.contractEnd = contractEnd;
    }

    public Company getCompany() {
        return company;
    }

    public void setCompany(Company company) {
        this.company = company;
    }

    public List<Cargo> getCargos() {
        return cargos;
    }

    public void setCargos(List<Cargo> cargos) {
        this.cargos = cargos;
    }
}
