package com.leyou.pojo;

import lombok.Data;

import javax.persistence.*;

@Data
@Table(name = "tb_spec_param")
public class SpecParam {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long cid;
    private Long groupId;
    private String name;
    @Column(name = "`numeric`")
    private Boolean numeric;
    private String unit;
    private Boolean generic;
    private Boolean searching;
    private String segments;

    @Override
    public String toString() {
        return "SpecParam{" +
                "id=" + id +
                ", cid=" + cid +
                ", groupId=" + groupId +
                ", name='" + name + '\'' +
                ", numeric=" + numeric +
                ", unit='" + unit + '\'' +
                ", generic=" + generic +
                ", searching=" + searching +
                ", segments='" + segments + '\'' +
                '}';
    }
}
