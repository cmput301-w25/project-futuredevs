package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.widget.FrameLayout;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProvider;

import com.futuredevs.models.ViewModelComments;
import com.futuredevs.models.items.MoodPost;
import com.google.android.material.appbar.MaterialToolbar;

/***
 * <p>The {@code ViewMoodUserActivity} class is an activity that handles
 * interactions for viewing moods, viewing profiles, and viewing comments
 * of posts.</p>
 *
 * <p>Provides a method to display the appropriate fragments and updating
 * the title of the toolbar when interactions happen.</p>
 *
 * @author Spencer Schmidt
 */
public class ViewMoodUserActivity extends AppCompatActivity {
	private MaterialToolbar toolbar;
	/** The fragment ID for a fragment of type {@link ViewMoodFragment}. */
	public static final int MOOD_FRAGMENT = 0;
	/** The fragment ID for a fragment of type {@link ViewProfileFragment}. */
	public static final int PROFILE_FRAGMENT = 1;
	/** The fragment ID for a fragment of type {@link ViewCommentFragment}. */
	public static final int COMMENT_FRAGMENT = 2;

	/**
	 * Initializes the view components for the activity and reads the provided
	 * intent arguments that were passed into the activity.
	 */
	@Override
	protected void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.activity_view_mood_user);
		this.toolbar = this.findViewById(R.id.toolbar_mood_user);
		this.setSupportActionBar(this.toolbar);

		if (this.getSupportActionBar() != null)  {
			this.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		}

		this.toolbar.setNavigationOnClickListener(v -> onBackPressed());
		Intent intent = this.getIntent();

		if (intent != null) {
			// We do not add these fragments to the backstack in order to allow
			// the back button to take the user to the previous activity instead
			// of popping the fragment off first.
			if (intent.hasExtra("view_post")) {
				MoodPost post = intent.getParcelableExtra("post");
				ViewMoodFragment moodFragment = ViewMoodFragment.newInstance(post);
				this.setFragment(MOOD_FRAGMENT, moodFragment, false);
			}
			else if (intent.hasExtra("user_profile")) {
				String username = intent.getStringExtra("name");
				ViewProfileFragment profileFragment = ViewProfileFragment.newInstance(username);
				this.setFragment(PROFILE_FRAGMENT, profileFragment, false);
			}
		}

		// Only here to create a view model for the fragments.
		new ViewModelProvider(this).get(ViewModelComments.class);

		// Because we use one toolbar for the activity, we must set the title
		// when the backstack changes.
		this.getSupportFragmentManager().addOnBackStackChangedListener(() -> {
			Fragment fragment = getSupportFragmentManager()
									.findFragmentById(R.id.frame_view_mood_user);

			if (fragment instanceof ViewMoodFragment) {
				toolbar.setTitle("Viewing Mood");
			}
			else if (fragment instanceof ViewProfileFragment) {
				toolbar.setTitle("Viewing Profile");
			}
			else if (fragment instanceof ViewCommentFragment) {
				toolbar.setTitle("Viewing Comment");
			}
		});
	}

	/**
	 * Sets the fragment be shown to the given {@code fragment}, sets the title
	 * of the toolbar based on {@code fragmentID}, and only adds the fragment
	 * to the back stack if {@code backStack} is {@code true}. The values that
	 * may be used for the {@code fragmentID} are {@link #MOOD_FRAGMENT},
	 * {@link #PROFILE_FRAGMENT}, and {@link #COMMENT_FRAGMENT}.
	 *
	 * @param fragmentID the ID of the fragment for setting the title
	 * @param fragment	 the fragment to display
	 * @param backStack  if {@code true}, adds the fragment to the backstack
	 */
	private void setFragment(int fragmentID, Fragment fragment, boolean backStack) {
		if (fragmentID == MOOD_FRAGMENT) {
			this.toolbar.setTitle("Viewing Mood");
		}
		else if (fragmentID == PROFILE_FRAGMENT) {
			this.toolbar.setTitle("Viewing Profile");
		}
		else if (fragmentID == COMMENT_FRAGMENT) {
			this.toolbar.setTitle("Viewing Comment");
		}

		FragmentTransaction transaction = this.getSupportFragmentManager().beginTransaction();
		transaction.replace(R.id.frame_view_mood_user, fragment);

		if (backStack) {
			transaction.addToBackStack(null);
		}

		transaction.commit();
	}

	/**
	 * Sets the fragment to be shown to the given {@code fragment} and sets the
	 * title of the toolbar based on {@code fragmentID}. The values that may be
	 * used for the {@code fragmentID} are {@link #MOOD_FRAGMENT},
	 * {@link #PROFILE_FRAGMENT}, and {@link #COMMENT_FRAGMENT}.
	 *
	 * @param fragmentID the ID of the fragment for setting the title
	 * @param fragment	 the fragment to display
	 */
	public void setFragment(int fragmentID, Fragment fragment) {
		this.setFragment(fragmentID, fragment, true);
	}
}