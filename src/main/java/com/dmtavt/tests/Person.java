package com.dmtavt.tests;

public class Person {
    public final int id;
    public String first;
    public String last;
    public String middle;
    public int dobYear;
    public int dobMonth;
    public String gender;
    public String salaryCurrency;
    public int salaryAmount;
    public String street;
    public String city;

    public Person(int id) {
        this.id = id;
    }

    public boolean isComplete() {
        return id != 0 && dobYear > 0 && dobMonth > 0 && salaryAmount > 0
                && first != null && last != null && middle != null
                && gender != null && salaryCurrency != null && city != null && street != null;
    }
}
