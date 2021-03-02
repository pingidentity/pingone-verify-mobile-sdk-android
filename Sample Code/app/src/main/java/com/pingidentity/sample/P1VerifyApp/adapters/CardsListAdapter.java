package com.pingidentity.sample.P1VerifyApp.adapters;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.pingidentity.sample.P1VerifyApp.adapters.viewholders.CardViewHolder;
import com.pingidentity.sample.P1VerifyApp.callbacks.DocumentClickListener;
import com.pingidentity.sample.P1VerifyApp.databinding.CardItemBinding;
import com.pingidentity.sample.P1VerifyApp.models.CardItem;

import java.util.List;

public class CardsListAdapter extends RecyclerView.Adapter<CardViewHolder> {

    public static final String TAG = CardsListAdapter.class.getCanonicalName();

    private List<CardItem> mCardsList;
    private DocumentClickListener mCallback;

    public CardsListAdapter(final List<CardItem> cardsList, DocumentClickListener callback) {
        this.mCardsList = cardsList;
        this.mCallback = callback;
    }

    @NonNull
    @Override
    public CardViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        final LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        CardItemBinding binding = CardItemBinding.inflate(inflater, parent, false);
        return new CardViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull CardViewHolder holder, int position) {
        holder.bind(mCardsList.get(position), mCallback);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemCount() {
        return mCardsList.size();
    }

}
