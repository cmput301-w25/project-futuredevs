package com.example.moodmento;

import android.app.Dialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;

import com.futuredevs.database.Database;
import com.futuredevs.models.ViewModelComments;
import com.futuredevs.models.items.MoodComment;
import com.futuredevs.models.items.MoodPost;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.textfield.TextInputLayout;

public class CommentBottomSheet extends BottomSheetDialogFragment {
	private Button postButton;
	private TextInputLayout replyLayout;
	private EditText replyText;
	private TextView replyingToView;
	private MoodPost post;
	private MoodComment comment;
	private ViewModelComments commentsModel;

	public static CommentBottomSheet newInstance(MoodPost parentPost) {
		CommentBottomSheet bottomSheet = new CommentBottomSheet();
		Bundle arguments = new Bundle();
		arguments.putParcelable("post", parentPost);
		bottomSheet.setArguments(arguments);
		return bottomSheet;
	}

	public static CommentBottomSheet newInstance(MoodComment parentComment) {
		CommentBottomSheet bottomSheet = new CommentBottomSheet();
		Bundle arguments = new Bundle();
		arguments.putParcelable("comment", parentComment);
		bottomSheet.setArguments(arguments);
		return bottomSheet;
	}

	@Nullable
	@Override
	public View onCreateView(@NonNull LayoutInflater inflater,
							 @Nullable ViewGroup container,
							 @Nullable Bundle savedInstanceState)
	{
		View view = inflater.inflate(R.layout.sheet_comment, container, false);
		this.replyingToView = view.findViewById(R.id.text_comment_sheet_replying_to);
		String replyingToStr = "Replying to %s";

		if (this.post != null) {
			this.replyingToView.setText(String.format(replyingToStr, this.post.getUser()));
		}
		else {
			this.replyingToView.setText(String.format(replyingToStr, this.comment.getPosterName()));
		}

		this.replyLayout = view.findViewById(R.id.layout_text_comment_sheet_text);
		this.replyText = view.findViewById(R.id.edittext_comment_text);
		this.replyText.addTextChangedListener(new TextWatcher() {
			@Override
			public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

			@Override
			public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

			@Override
			public void afterTextChanged(Editable editable) {
				Editable replyStr = replyText.getText();

				if (replyStr.length() <= 0) {
					postButton.setEnabled(false);
				}
				else {
					postButton.setEnabled(replyStr.length() <= replyLayout.getCounterMaxLength());
				}
			}
		});

		this.postButton = view.findViewById(R.id.button_comment_post);
		this.postButton.setEnabled(false);
		this.postButton.setOnClickListener(v -> {
			String userName = Database.getInstance().getCurrentUser();
			String commentText = replyText.getText().toString();

			if (post != null) {
				MoodComment moodComment = new MoodComment(post, userName, commentText);
				commentsModel.postTopLevelComment(moodComment);
			}
			else {
				MoodComment moodComment = new MoodComment(comment, userName, commentText);
				commentsModel.postSubComment(moodComment);
			}

			dismiss();
		});

		this.commentsModel = new ViewModelProvider(this.requireActivity()).get(ViewModelComments.class);
		return view;
	}

	@Override
	public void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.setStyle(STYLE_NORMAL, R.style.CommentBottomSheet);

		if (this.getArguments() != null) {
			if (this.getArguments().containsKey("post")) {
				this.post = this.getArguments().getParcelable("post");
			}

			if (this.getArguments().containsKey("comment")) {
				this.comment = this.getArguments().getParcelable("comment");
			}
		}
	}

	@NonNull
	@Override
	public Dialog onCreateDialog(@Nullable Bundle savedInstanceState)
	{
		BottomSheetDialog dialog = new BottomSheetDialog(this.getContext());
//		dialog.setContentView(R.layout.sheet_comment);
		return dialog;
	}
}