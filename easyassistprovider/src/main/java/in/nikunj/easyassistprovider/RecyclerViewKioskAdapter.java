package in.nikunj.easyassistprovider;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.util.List;

public class RecyclerViewKioskAdapter extends RecyclerView.Adapter<RecyclerViewKioskAdapter.RecyclerViewKioskAdapterViewHolder> {
    private   KioskItemListener kioskItemListener;
    private List<KioskDataSet> kioskDataSets = null;

    public RecyclerViewKioskAdapter(KioskItemListener kioskItemListener, List<KioskDataSet> kioskDataSets) {
        this.kioskDataSets = kioskDataSets;
        this.kioskItemListener = kioskItemListener;
    }

    @Override
    public RecyclerViewKioskAdapterViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        final View inflate = LayoutInflater.from(parent.getContext()).inflate(R.layout.kiosk_item, parent, false);
        return new RecyclerViewKioskAdapterViewHolder(inflate);
    }

    @Override
    public void onBindViewHolder(RecyclerViewKioskAdapterViewHolder holder, int position) {
        holder.kioskItemId.setText(kioskDataSets.get(position).getKioskName());

    }

    @Override
    public int getItemCount() {
        return kioskDataSets.size();
    }

    public List<KioskDataSet> getAllKioskData() {
        return kioskDataSets;
    }

    public class RecyclerViewKioskAdapterViewHolder extends RecyclerView.ViewHolder {
        private Button kioskItemId;

        public RecyclerViewKioskAdapterViewHolder(View itemView) {
            super(itemView);
            kioskItemId = (Button) itemView.findViewById(R.id.kiosk_item_id);
            kioskItemId.setOnClickListener(new AdapterView.OnClickListener() {
                @Override
                public void onClick(View v) {
                    kioskItemListener.itemOnClick(kioskDataSets.get(getAdapterPosition()));
                }
            });
        }
    }

    public interface KioskItemListener {
        void itemOnClick(KioskDataSet kioskDataSet);
    }
}
