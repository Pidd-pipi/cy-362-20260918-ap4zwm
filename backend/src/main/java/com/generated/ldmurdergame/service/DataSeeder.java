package com.generated.ldmurdergame.service;

import java.util.List;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import com.generated.ldmurdergame.mapper.DmMapper;
import com.generated.ldmurdergame.model.Dm;

@Component
public class DataSeeder implements ApplicationRunner {
  private final DmMapper dmMapper;

  public DataSeeder(DmMapper dmMapper) {
    this.dmMapper = dmMapper;
  }

  @Override
  public void run(ApplicationArguments args) {
    if (dmMapper.countAll() > 0) {
      return;
    }
    seed("林晚", List.of("硬核推理", "情感沉浸"));
    seed("顾言", List.of("硬核推理", "恐怖氛围"));
    seed("苏蔓", List.of("情感沉浸", "欢乐机制"));
    seed("陈默", List.of("欢乐机制", "恐怖氛围"));
    seed("陆离", List.of("硬核推理", "欢乐机制", "情感沉浸"));
  }

  private void seed(String name, List<String> skills) {
    Dm dm = new Dm();
    dm.setName(name);
    dmMapper.insert(dm);
    for (String skill : skills) {
      dmMapper.insertSkill(dm.getId(), skill);
    }
  }
}
