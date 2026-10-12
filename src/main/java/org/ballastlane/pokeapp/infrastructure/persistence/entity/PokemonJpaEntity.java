package org.ballastlane.pokeapp.infrastructure.persistence.entity;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OrderColumn;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import org.hibernate.annotations.BatchSize;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Entity
@Table(name = "pokemon")
public class PokemonJpaEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "pokeapi_id", unique = true)
  private Integer pokeApiId;

  @Column(nullable = false, length = 100)
  private String name;

  @Column(length = 100)
  private String localName;

  @Column(length = 50)
  private String region;

  @Column(length = 100)
  private String category;

  @Column(name = "height_m", nullable = false)
  private double heightM;

  @Column(name = "weight_kg", nullable = false)
  private double weightKg;

  @Column(length = 500)
  private String spriteUrl;

  @Column(length = 1000)
  private String description;

  @ElementCollection
  @CollectionTable(name = "pokemon_type", joinColumns = @JoinColumn(name = "pokemon_id"))
  @OrderColumn(name = "position")
  @Column(name = "type_name", nullable = false, length = 30)
  @BatchSize(size = 100)
  private List<String> types = new ArrayList<>();

  @ElementCollection
  @CollectionTable(name = "pokemon_ability", joinColumns = @JoinColumn(name = "pokemon_id"))
  @OrderColumn(name = "position")
  @Column(name = "ability_name", nullable = false, length = 50)
  @BatchSize(size = 100)
  private List<String> abilities = new ArrayList<>();

  @ElementCollection
  @CollectionTable(name = "pokemon_tag", joinColumns = @JoinColumn(name = "pokemon_id"))
  @OrderColumn(name = "position")
  @Column(name = "tag", nullable = false, length = 30)
  @BatchSize(size = 100)
  private List<String> tags = new ArrayList<>();

  @Version
  @Column(nullable = false)
  private Long version;

  @CreationTimestamp
  @Column(nullable = false, updatable = false)
  private Instant createdAt;

  @UpdateTimestamp
  @Column(nullable = false)
  private Instant updatedAt;

  public Long getId() {
    return id;
  }

  public Integer getPokeApiId() {
    return pokeApiId;
  }

  public void setPokeApiId(Integer pokeApiId) {
    this.pokeApiId = pokeApiId;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getLocalName() {
    return localName;
  }

  public void setLocalName(String localName) {
    this.localName = localName;
  }

  public String getRegion() {
    return region;
  }

  public void setRegion(String region) {
    this.region = region;
  }

  public String getCategory() {
    return category;
  }

  public void setCategory(String category) {
    this.category = category;
  }

  public double getHeightM() {
    return heightM;
  }

  public void setHeightM(double heightM) {
    this.heightM = heightM;
  }

  public double getWeightKg() {
    return weightKg;
  }

  public void setWeightKg(double weightKg) {
    this.weightKg = weightKg;
  }

  public String getSpriteUrl() {
    return spriteUrl;
  }

  public void setSpriteUrl(String spriteUrl) {
    this.spriteUrl = spriteUrl;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public List<String> getTypes() {
    return types;
  }

  public void replaceTypes(List<String> values) {
    types.clear();
    types.addAll(values);
  }

  public List<String> getAbilities() {
    return abilities;
  }

  public void replaceAbilities(List<String> values) {
    abilities.clear();
    abilities.addAll(values);
  }

  public List<String> getTags() {
    return tags;
  }

  public void replaceTags(List<String> values) {
    tags.clear();
    tags.addAll(values);
  }

  public Long getVersion() {
    return version;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }

  public Instant getUpdatedAt() {
    return updatedAt;
  }
}
