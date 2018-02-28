package com.example.asus.onlinecanteen.adapter;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.example.asus.onlinecanteen.R;
import com.example.asus.onlinecanteen.model.Transaction;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Steven Albert on 2/17/2018.
 */

public class TransactionHistoryAdapter extends RecyclerView.Adapter<TransactionHistoryAdapter.ViewHolder> {

    // Transaction History Items
    private ArrayList<Transaction> transactionHistory;

    /**
     * Construct {@link TransactionHistoryAdapter} instance
     * @param history list of transaction history
     */
    public TransactionHistoryAdapter(@NonNull List<Transaction> history) {
        this.transactionHistory = new ArrayList<>(history);
    }

    /**
     * Create {@link ViewHolder} instance of the views
     * @param parent ViewGroup instance in which the View is added to
     * @param viewType the view type of the new View
     * @return new ViewHolder instance
     */
    @Override public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View layoutView = LayoutInflater.from(parent.getContext()).inflate(R.layout.history_adapter_item, parent, false);

        return new ViewHolder(layoutView);
    }

    /**
     * Bind the view with data at the specified position
     * @param holder ViewHolder which should be updated
     * @param position position of items in the adapter
     */
    @Override public void onBindViewHolder(ViewHolder holder, int position) {
        // Get Transaction Item
        Transaction transaction = transactionHistory.get(position);
        // Set Information on View
        holder.storeNameTextView.setText(transaction.getSid());
        holder.transactionDateTextView.setText(String.valueOf(transaction.getPurchaseDate()));
        holder.paymentAmountTextView.setText("Rp " + String.valueOf(transaction.getTotalPrice()));
    }

    /**
     * Retrieved the amount of items in adapter
     * @return amount of items in adapter
     */
    @Override public int getItemCount() {
        return transactionHistory.size();
    }

    /**
     * ViewHolder class of {@link TransactionHistoryAdapter}
     */
    public static class ViewHolder extends RecyclerView.ViewHolder {

        // TextView of store name
        public TextView storeNameTextView;
        // TextView of transaction date
        public TextView transactionDateTextView;
        // TextView of payment amount
        public TextView paymentAmountTextView;

        /**
         * Construct {@link ViewHolder} instance
         * @param view layout view of transaction items
         */
        public ViewHolder(View view) {
            super(view);
            // Set the holder attributes
            storeNameTextView = view.findViewById(R.id.history_item_store_name);
            transactionDateTextView = view.findViewById(R.id.history_item_transaction_date);
            paymentAmountTextView = view.findViewById(R.id.history_item_payment_amount);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override public void onClick(View v) {
                    Integer pos = getAdapterPosition();
                    Toast.makeText(itemView.getContext(), pos.toString() , Toast.LENGTH_SHORT).show();
                }
            });
        }
    }
}
