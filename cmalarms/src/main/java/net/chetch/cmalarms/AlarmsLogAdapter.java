package net.chetch.cmalarms;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import net.chetch.cmalarms.data.AlarmsLog;
import net.chetch.cmalarms.data.AlarmsLogEntry;

import androidx.recyclerview.widget.RecyclerView;

public class AlarmsLogAdapter extends RecyclerView.Adapter<AlarmsLogAdapter.ViewHolder> {

    public static class ViewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case
        View contentView;
        AlarmsLogEntryFragment logEntryFragment;

        public ViewHolder(View v, AlarmsLogEntryFragment logEntryFragment) {
            super(v);
            contentView = v;
            this.logEntryFragment = logEntryFragment;
        }
    }

    public AlarmsLog entries;

    // Provide a suitable constructor (depends on the kind of dataset)
    //public AlarmsLogAdapter(MainActivity mainActivity) {
      //  this.mainActivity = mainActivity;
    //}

    public void setDataset(AlarmsLog entries) {
        this.entries = entries;
        notifyDataSetChanged();
    }

    // Create new views (invoked by the layout manager)
    @Override
    public AlarmsLogAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // create a new view
        AlarmsLogEntryFragment lef = new AlarmsLogEntryFragment();
        //lef.populateOnCreate = false;
        View v = lef.onCreateView(LayoutInflater.from(parent.getContext()), parent, null);

        ViewHolder vh = new ViewHolder(v, lef);
        return vh;
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        // - get element from your dataset at this position
        // - replace the contents of the view with that element

        Log.i("ALA", "Binding view holder");

        if (entries != null) {
            AlarmsLogEntry entry = entries.get(position);
            holder.logEntryFragment.populateContent(entry);

            //Log.i("LEAdapter", "Binding view holder at pos " + position + " for " + holder.logEntryFragment.crewMember.getFullName());
        }
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return entries == null ? 0 : entries.size();
    }
}
