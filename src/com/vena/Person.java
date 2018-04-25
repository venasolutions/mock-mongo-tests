package com.vena;

import org.mongodb.morphia.annotations.Id;

public class Person {
	static private long NEXT_ID = 1L;

	@Id
	private long id;
	private String name;
	private String bday;
	private double accountBalance;

	public Person(String name, String bday, double accountBalance) {
		this.id = ++NEXT_ID;  // auto-incremented ID
		this.name = name;
		this.bday = bday;
		this.accountBalance = accountBalance;
	}

	// morphia needs this one to deserialize things
	public Person() {}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getBday() {
		return bday;
	}

	public void setBday(String bday) {
		this.bday = bday;
	}

	public double getAccountBalance() {
		return accountBalance;
	}

	public void setAccountBalance(double accountBalance) {
		this.accountBalance = accountBalance;
	}
}
