package com.finaltest.Model;

public class User {
    private String Name, Password, Phone, IsStaff;

    public User(String name, String password, String phone, String isStaff) {
        Name = name;
        Password = password;
        Phone = phone;
        IsStaff = isStaff;
    }

    public User(String name, String password) {
        Name = name;
        Password = password;
    }

    public void setName(String name) {
        Name = name;
    }

    public void setPassword(String password) {
        Password = password;
    }

    public void setPhone(String phone) {
        Phone = phone;
    }

    public void setIsStaff(String isStaff) {
        IsStaff = isStaff;
    }

    public User() {
    }

    public String getName() {
        return Name;
    }

    public String getPassword() {
        return Password;
    }

    public String getPhone() {
        return Phone;
    }

    public String getIsStaff() {
        return IsStaff;
    }
}
