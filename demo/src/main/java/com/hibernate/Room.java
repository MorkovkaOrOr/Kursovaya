package com.hibernate;

import javax.persistence.*;
import java.util.List;

@Entity
@Table(name = "room")
public class Room {

    @Id
    @Column(name = "idroom")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int roomId;

    // Связь "один ко многим" с Shelf
    @OneToMany(mappedBy = "room", cascade = CascadeType.ALL)
    private List<Shelf> shelves;

    // Максимальное количество полок в комнате
    @Column(name = "maxshelves")
    private Integer maxShelves;

    public Room() {
    }

    // Геттеры и сеттеры
    public int getRoomId() {
        return roomId;
    }

    public void setRoomId(int roomId) {
        this.roomId = roomId;
    }

    public List<Shelf> getShelves() {
        return shelves;
    }

    public void setShelves(List<Shelf> shelves) {
        // Проверка на превышение максимального количества полок
        this.shelves = shelves;
    }

    public Integer getMaxShelves() {
        return maxShelves;
    }

    public void setMaxShelves(Integer maxShelves) {
        // Проверка на неотрицательное значение
        if (maxShelves != null && maxShelves <= 0) {
            throw new IllegalArgumentException("Maximum number of shelves cannot be negative or 0.");
        }
        this.maxShelves = maxShelves;
    }

    /**
     * Calculate the total occupied space in the room by summing up
     * the occupied space of all shelves in this room.
     *
     * @return the total occupied space
     */
    public int getOccupiedSpace() {
        int totalOccupiedSpace = 0;

        if (shelves != null) {
            for (Shelf shelf : shelves) {
                if (shelf.getCargos() != null) {
                    for (Cargo cargo : shelf.getCargos()) {
                        totalOccupiedSpace += cargo.getQuantity();
                    }
                }
            }
        }

        return totalOccupiedSpace;
    }
    public int getFreeShelfSpace() {
        if (maxShelves == null) {
            throw new IllegalStateException("Maximum shelf count is not set for this room.");
        }

        // Считаем количество полок в комнате
        int currentShelfCount = (shelves != null) ? shelves.size() : 0;

        // Рассчитываем свободное место для полок
        return maxShelves - currentShelfCount;
    }
}