package com.example.myapplication;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.futuredevs.models.items.MoodPost;

import java.util.List;

public class ViewModelUserMoods extends ViewModel {
	private MutableLiveData<List<MoodPost>> moodData = new MutableLiveData<>();

	public void setMoodData(List<MoodPost> posts) {
		this.moodData.setValue(posts);
	}

	public List<MoodPost> getPostData() {
		return this.moodData.getValue();
	}

	public MutableLiveData<List<MoodPost>> getData() {
		return this.moodData;
	}
}