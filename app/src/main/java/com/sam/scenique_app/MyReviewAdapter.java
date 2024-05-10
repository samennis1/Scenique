package com.sam.scenique_app;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;

public class MyReviewAdapter extends RecyclerView.Adapter<MyReviewAdapter.MyReviewHolder>{

    private List<LocationReview> reviews;
    public void setReviews(List<LocationReview> reviews) {
        this.reviews = reviews;
        notifyDataSetChanged();
    }


    @NonNull
    @Override
    public MyReviewAdapter.MyReviewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycle_view, parent, false);
        return new MyReviewAdapter.MyReviewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyReviewAdapter.MyReviewHolder holder, int position) {

    }

    @Override
    public int getItemCount() {
        return reviews != null ? reviews.size() : 0;
    }

    static class MyReviewHolder extends RecyclerView.ViewHolder {
        private ImageView photoImageView;
        private TextView reviewTextView;
        private TextView locationTextView;

        MyReviewHolder(View itemView) {
            super(itemView);
            photoImageView = itemView.findViewById(R.id.imageView_review_image);
            reviewTextView = itemView.findViewById(R.id.textView_review_description);
            locationTextView = itemView.findViewById(R.id.textView_location);
        }

        @SuppressLint("SetTextI18n")
        void bind(LocationReview review){
            Glide.with(itemView.getContext())
                    .load(review.getPhotoUrl())
                    .into(photoImageView);
            String reviewText = String.valueOf(new StringBuilder()
                    .append(review.getReviewText().substring(0, Math.min(review.getReviewText().length(), 30)))
                    .append("..."));
            reviewTextView.setText(reviewText);
            locationTextView.setText(review.getLongitude() + "" + review.getLatitude());
        }
    }
}
