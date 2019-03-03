package es.joapingut.eshoe;

import android.bluetooth.BluetoothDevice;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

public class ScanResultAdapter extends RecyclerView.Adapter<ScanResultAdapter.ScanResultHolder>{

    public interface OnItemClickListener {
        void onItemClick(View itemView, BluetoothDevice device);
    }

    private List<BluetoothDevice> scanResults;
    private OnItemClickListener listener;

    public ScanResultAdapter(List<BluetoothDevice> scanResults){
        this.scanResults = scanResults;
    }

    @NonNull
    @Override
    public ScanResultHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View v1 = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.recycler_view_item, viewGroup, false);
        ScanResultHolder holder = new ScanResultHolder(v1);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull ScanResultHolder scanResultHolder, int i) {
        scanResultHolder.textViewUpper.setText(scanResults.get(i).getName());
        scanResultHolder.textViewDown.setText(scanResults.get(i).getAddress());
    }

    @Override
    public int getItemCount() {
        return scanResults.size();
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    public class ScanResultHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        public TextView textViewUpper;
        public TextView textViewDown;
        public ImageView imageView;

        public ScanResultHolder(View v1) {
            super(v1);
            textViewUpper = v1.findViewById(R.id.scanItemTxtUpper);
            textViewDown = v1.findViewById(R.id.scanItemTxtDown);
            imageView = v1.findViewById(R.id.scanItemImage);
            v1.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            if(listener != null){
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION){
                    listener.onItemClick(this.itemView, scanResults.get(position));
                }
            }
        }
    }
}
