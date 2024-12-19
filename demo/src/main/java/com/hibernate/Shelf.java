package com.hibernate;

import javax.persistence.*;
import java.util.List;

@Entity
@Table(name = "shelf")
class Shelf {

    @Id
    @Column(name = "idshelf")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int shelfId;

    @Column(name = "quantity")
    private int count;

    // Связь "один ко многим" с Cargo
    @OneToMany(mappedBy = "shelf", cascade = CascadeType.ALL)
    private List<Cargo> cargos;

    // Связь "многие к одному" с Room
    @ManyToOne
    @JoinColumn(name = "idroom", referencedColumnName = "idroom")  // связываем по колонке idroom
    private Room room;

    public Shelf() {
    }

    // Геттеры и сеттеры
    public int getShelfId() {
        return shelfId;
    }

    public void setShelfId(int shelfId) {
        this.shelfId = shelfId;
    }

    public int getQuantity() {
        return count;
    }

    public void setQuantity(int count) {
        this.count = count;
    }

    public List<Cargo> getCargos() {
        return cargos;
    }

    public void setCargos(List<Cargo> cargos) {
        this.cargos = cargos;
    }

    public Room getRoom() {
        return room;
    }

    public void setRoom(Room room) {
        this.room = room;
    }

    public int getAvailableSpace() {
        int totalOccupiedSpace = 0;
        if (cargos != null) {
            for (Cargo cargo : cargos) {
                totalOccupiedSpace += cargo.getQuantity();
            }
        }
        return count - totalOccupiedSpace;
    }
}
