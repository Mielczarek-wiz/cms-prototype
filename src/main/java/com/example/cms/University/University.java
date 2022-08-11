package com.example.cms.University;

import javax.persistence.*;

@Entity
@Table
public class University {

    @Id
    @SequenceGenerator(
            name = "university_sequence",
            sequenceName = "university_sequence",
            allocationSize = 1
    )
    @GeneratedValue(
            generator = "university_sequence",
            strategy = GenerationType.SEQUENCE
    )
    private Long id;
    private String name;
    private String shortName;
    private Boolean isHidden;

    public University(){

    }
    public University(String name,
                           String shortName,
                           Boolean isHidden){
        this.name = name;
        this.shortName = shortName;
        this.isHidden = isHidden;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getShortName() {
        return shortName;
    }

    public void setShortName(String shortName) {
        this.shortName = shortName;
    }

    public Boolean getHidden() {
        return isHidden;
    }

    public void setHidden(Boolean hidden) {
        isHidden = hidden;
    }

    @Override
    public String toString() {
        return "University{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", shortName='" + shortName + '\'' +
                ", isHidden=" + isHidden +
                '}';
    }
}