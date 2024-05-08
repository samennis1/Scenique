package com.sam.scenique_app;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import java.util.List;

public class ReviewAdapter extends RecyclerView.Adapter<ReviewAdapter.ReviewViewHolder> {

    private List<Review> reviews;

    public void setReviews(List<Review> reviews) {
        this.reviews = reviews;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ReviewViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.review_item, parent, false);
        return new ReviewViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ReviewViewHolder holder, int position) {
        Review review = reviews.get(position);
        holder.bind(review);
    }

    @Override
    public int getItemCount() {
        return reviews != null ? reviews.size() : 0;
    }

    static class ReviewViewHolder extends RecyclerView.ViewHolder {
        private ImageView photoImageView;
        private TextView reviewTextView;
        private RatingBar ratingBar;

        ReviewViewHolder(View itemView) {
            super(itemView);
            photoImageView = itemView.findViewById(R.id.review_photo);
            reviewTextView = itemView.findViewById(R.id.review_text);
            ratingBar = itemView.findViewById(R.id.review_rating);
        }

        void bind(Review review) {
            Glide.with(itemView.getContext())
                    .load(review.getPhotoUrl())
                    .into(photoImageView);
            reviewTextView.setText(review.getReviewText());
            ratingBar.setRating(review.getRating());
        }
    }
}
