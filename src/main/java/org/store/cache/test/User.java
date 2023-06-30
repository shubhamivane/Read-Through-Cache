package org.store.cache.test;

public class User {
  private String name;
  private String lastName;

  public String getName() {
    return name;
  }

  public String getLastName() {
    return lastName;
  }

  public void setName(String name) {
    this.name = name;
  }

  public void setLastName(String lastName) {
    this.lastName = lastName;
  }

  @Override
  public String toString() {
    return "User{" +
        "name='" + name + '\'' +
        ", lastName='" + lastName + '\'' +
        '}';
  }
}
