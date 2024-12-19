package com.hibernate;

import javax.persistence.*;

@Entity
@Table(name = "object")
class Cargo {

    @Id
    @Column(name = "idobject")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int idobject;

    @Column(name = "objectname")
    private String objectName;

    @Column(name = "quantitycargo")
    private Integer count;
    
    // Связь "многие к одному" с Shelf
    @ManyToOne
    @JoinColumn(name = "IDshelf", referencedColumnName = "idshelf")
    private Shelf shelf;

    // Связь "многие к одному" с Contract
    @ManyToOne
    @JoinColumn(name = "IDcontract", referencedColumnName = "idcontract")
    private Contract contract;

    // Конструкторы, геттеры и сеттеры
    public Cargo() {}

    public Cargo(String name, Shelf shelf, Contract contract) {
        this.objectName = name;
        this.shelf = shelf;
        this.contract = contract;
    }

    public int getIdObject() {
        return idobject;
    }

    public void setIdObject(int idobject) {
        this.idobject = idobject;
    }

    public Integer getQuantity() {
        return count;
    }

    public void setQuantity(Integer count) {
        this.count = count;
    }

    public String getObjectName() {
        return objectName;
    }

    public void setObjectName(String objectName) {
        this.objectName = objectName;
    }

    public Shelf getShelf() {
        return shelf;
    }

    public void setShelf(Shelf shelf) {
        this.shelf = shelf;
    }

    public Contract getContract() {
        return contract;
    }

    public void setContract(Contract contract) {
        this.contract = contract;
    }
}
