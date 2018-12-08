package com.qh.common.domain;

import java.awt.geom.Area;
import java.io.Serializable;
import java.util.List;

public class CityDO implements Serializable{
    /**
	 */
	private static final long serialVersionUID = 1L;
	private String id;
    private String name;
    private List<Area> areas;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Area> getAreas() {
        return areas;
    }

    public void setAreas(List<Area> areas) {
        this.areas = areas;
    }
}
