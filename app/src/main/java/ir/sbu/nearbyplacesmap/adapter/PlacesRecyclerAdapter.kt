package ir.sbu.nearbyplacesmap.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.*
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import ir.sbu.nearbyplacesmap.R
import ir.sbu.nearbyplacesmap.model.Place
import kotlinx.android.synthetic.main.recycler_place_item.view.*

class PlacesRecyclerAdapter
    : RecyclerView.Adapter<PlacesRecyclerAdapter.PlaceViewHolder>(){

    private val diffCallback = object : DiffUtil.ItemCallback<Place>() {

        override fun areItemsTheSame(oldItem: Place, newItem: Place): Boolean {
            return oldItem.title == newItem.title
        }

        override fun areContentsTheSame(oldItem: Place, newItem: Place): Boolean {
            return oldItem.title == newItem.title
        }
    }

    private lateinit var myRecyclerView: RecyclerView

    private val differ =
        AsyncListDiffer(
            RecyclerChangeCallback(this),
            AsyncDifferConfig.Builder(diffCallback).build()
        )


    fun submitList(tabTitleList: List<Place>?){
        differ.submitList(tabTitleList)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlaceViewHolder {
        val view = LayoutInflater
            .from(parent.context)
            .inflate(R.layout.recycler_place_item, parent, false)
        return PlaceViewHolder(view)
    }

    override fun getItemCount(): Int {
        return differ.currentList.size
    }

    override fun onBindViewHolder(holder: PlaceViewHolder, position: Int) {
        holder.bind(differ.currentList[position])
    }

    class PlaceViewHolder(itemView: View): RecyclerView.ViewHolder(itemView){

        private val placeTitleTextView = itemView.placeTitleTextView
        private val placeImageView = itemView.placeImage

        fun bind(place: Place) {
            placeTitleTextView.text = place.title

            var requestOptions = RequestOptions()
            requestOptions = requestOptions.transform(CenterCrop(), RoundedCorners(25))
            Glide
                .with(itemView.context)
                .load(R.drawable.restaurant_sample)
                .apply(requestOptions)
                .into(placeImageView)
        }

    }

    private inner class RecyclerChangeCallback(
        private val adapter: PlacesRecyclerAdapter
    ) : ListUpdateCallback {

        override fun onChanged(position: Int, count: Int, payload: Any?) {
//            myRecyclerView.smoothScrollToPosition(0)
            adapter.notifyItemRangeChanged(position, count, payload)
        }

        override fun onInserted(position: Int, count: Int) {
            myRecyclerView.smoothScrollToPosition(0)
            adapter.notifyItemRangeChanged(position, count)
        }

        override fun onMoved(fromPosition: Int, toPosition: Int) {
            adapter.notifyDataSetChanged()
        }

        override fun onRemoved(position: Int, count: Int) {
            myRecyclerView.smoothScrollToPosition(0)
            adapter.notifyDataSetChanged()
        }
    }

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)
        this.myRecyclerView = recyclerView
    }
}