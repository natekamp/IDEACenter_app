package natekamp.ideas;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

public class SubjectListRecyclerAdapter extends RecyclerView.Adapter<SubjectListRecyclerAdapter.ViewHolder>{

    private List<String> mStrings;
    private List<Integer> mInts;
    private LayoutInflater mInflater;
    private ItemClickListener mClickListener;
    Context context;

    SubjectListRecyclerAdapter(Context context, List<String> strings, List<Integer> ints) {
        this.mInflater = LayoutInflater.from(context);
        this.mStrings = strings;
        this.mInts = ints;
        this.context = context;
    }

    // inflates the row layout from xml when needed
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.subject_image_card, parent, false);
        return new ViewHolder(view);
    }

    // binds the data to the TextView and ImageView in each row
    @Override
    public void onBindViewHolder(ViewHolder holder, int position)
    {
        String title = mStrings.get(position);
        holder.subjectText.setText(title);

        if (mInts != null)
        {
            Integer image = mInts.get(position);
            holder.subjectImage.setImageResource(image);
        }
    }

    @Override
    public int getItemCount() {
        return mStrings.size();
    }


    // stores and recycles views as they are scrolled off screen
    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        ImageView subjectImage;
        TextView subjectText;

        ViewHolder(View itemView) {
            super(itemView);
            subjectImage = itemView.findViewById(R.id.card_image);
            subjectText = itemView.findViewById(R.id.card_text);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            if (mClickListener != null) mClickListener.onItemClick(view, getAdapterPosition());
        }
    }

    String getString(int id) {
        return mStrings.get(id);
    }
    Integer getThumbnail(int id) {
        return mInts.get(id);
    }

    // allows click events to be caught
    void setClickListener(ItemClickListener itemClickListener) {
        this.mClickListener = itemClickListener;
    }

    // parent activity will implement this method to respond to click events
    public interface ItemClickListener {
        void onItemClick(View view, int position);
    }

}
