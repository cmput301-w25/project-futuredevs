package com.futuredevs.database;

import com.google.firebase.firestore.FirebaseFirestore;

public class Database
{
	private FirebaseFirestore db;

	public Database() {
		this.db = FirebaseFirestore.getInstance();
	}


}