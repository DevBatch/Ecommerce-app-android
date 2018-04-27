package com.devbatch.ecommerce.fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import com.devbatch.ecommerce.R;
import com.devbatch.ecommerce.adapters.RecyclerViewArrayAdapter;

import java.util.List;


public class MyAddressesAdapter extends RecyclerViewArrayAdapter<MyAddressesModel, MyAddressesAdapter.MyViewHolder> {
    RecyclerView recyclerView;
    List<MyAddressesModel> addrList;

    public MyAddressesAdapter(List<MyAddressesModel> addrList) {
        this.addrList = addrList;
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView title, name, address, fex, phone;

        public MyViewHolder(View itemView) {
            super(itemView);
            title = (TextView) itemView.findViewById(R.id.title);
            name = (TextView) itemView.findViewById(R.id.name);
            address = (TextView) itemView.findViewById(R.id.address);
            fex = (TextView) itemView.findViewById(R.id.fex);
            phone = (TextView) itemView.findViewById(R.id.phone);

        }
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.fragment_addressess_row, parent, false);
        return new MyViewHolder(itemView);
    }


    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        MyAddressesModel addressesModel = addrList.get(position);
        holder.title.setText(addressesModel.getTitle());
        holder.name.setText(addressesModel.getName());
        holder.address.setText(addressesModel.getAddress());
        holder.fex.setText(addressesModel.getFex());
        holder.phone.setText(addressesModel.getPhone());

    }

    @Override
    public int getItemCount() {
        return addrList.size();
    }
}
