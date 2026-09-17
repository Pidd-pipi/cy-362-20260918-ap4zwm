package com.generated.ldmurdergame.model;

import java.util.ArrayList;
import java.util.List;

public class Dm {
  private Long id;
  private String name;
  private List<String> skills = new ArrayList<>();

  public Dm() {
  }

  public Dm(Long id, String name) {
    this.id = id;
    this.name = name;
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

  public List<String> getSkills() {
    return skills;
  }

  public void setSkills(List<String> skills) {
    this.skills = skills;
  }
}
