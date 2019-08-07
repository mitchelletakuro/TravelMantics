package com.novatech.travelmantics;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;


import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;


public class DealAdapter extends RecyclerView.Adapter<DealAdapter.TravelDealViewHolder> {

    private ArrayList<TravelDeal> deals;
    private FirebaseDatabase mFirebaseDb;
    private DatabaseReference dbReference;
    private ChildEventListener childEventListener;

    public DealAdapter() {
       // FirebaseUtil.openFirebaseRef("traveldeals");
      //  dbReference= mFirebaseDb.getReference().child("traveldeals");
        mFirebaseDb = FirebaseUtil.mFirebaseDb;
        dbReference= FirebaseUtil.mDbReference;
        this.deals =FirebaseUtil.mDeals;

        childEventListener = new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                //TextView tvDeals = (TextView) findViewbyId(R.id.title_tv_deal);
                TravelDeal travelDeal = dataSnapshot.getValue(TravelDeal.class);
                travelDeal.setId(dataSnapshot.getKey());
                deals.add(travelDeal);
                notifyItemInserted(deals.size() - 1);
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        };
        dbReference.addChildEventListener(childEventListener);
    }
      //  dbReference = FirebaseUtil.mDbReference;



        @NonNull
        @Override
        public TravelDealViewHolder onCreateViewHolder (@NonNull ViewGroup viewGroup, int i){
            Context context = viewGroup.getContext();
            View itemView = LayoutInflater.from(context).inflate(R.layout.list_item_single, viewGroup, false);
            return new TravelDealViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder (@NonNull TravelDealViewHolder travelDealViewHolder,int i){
            TravelDeal deal = deals.get(i);
            travelDealViewHolder.tvTitle.setText(deal.getTitle());
            travelDealViewHolder.tvDescription.setText(deal.getDescription());
            travelDealViewHolder.tvPrice.setText(deal.getPrice());
            FirebaseUtil.connectStorage();
            travelDealViewHolder.showImage(deal.getImageUrl());
        }

        @Override
        public int getItemCount () {
            return deals.size();
        }

        public class TravelDealViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
            CardView cardView;
            TextView tvTitle;
            TextView tvDescription;
            TextView tvPrice;
            CircleImageView imageDeal;
            Context context;

            public TravelDealViewHolder(@NonNull View itemView) {
                super(itemView);
                context = itemView.getContext();
                tvTitle = itemView.findViewById(R.id.title_tv_deal);
                tvDescription = itemView.findViewById(R.id.description_tv_deal);
                tvPrice = itemView.findViewById(R.id.price_tv_deal);
                imageDeal = itemView.findViewById(R.id.deal_image);
                itemView.setOnClickListener(this);
            }


            @Override
            public void onClick(View v) {
                int position = getAdapterPosition();
                TravelDeal selectedDeal = deals.get(position);
                Intent intent = new Intent(v.getContext(), AdminActivity.class);
                intent.putExtra("Deal", selectedDeal);
                v.getContext().startActivity(intent);
            }

            private void showImage(String url) {
                if (url != null && !url.isEmpty()) {
                    Picasso.get()
                            .load(url)
                            .resize(160, 160)
                            .centerCrop()
                            .into(imageDeal);
                }
            }
        }
}






