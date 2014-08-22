package com.example.ibeacontest;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class BeaconListAdapter extends BaseAdapter
{
	private Context mContext;
	  
	List<ListItem> mListItems= new ArrayList<ListItem>();

	/** ================================================ */
	public BeaconListAdapter(Context c) { mContext= c; }
	
	/** ================================================ */
	public int getCount() { return mListItems.size(); }
	
	/** ================================================ */
	public Object getItem(int position)
	{
		if((!mListItems.isEmpty()) && mListItems.size() > position)
		{
			return mListItems.toArray()[position];
		}
		
		return null;
	}
	  
	public String getItemText(int position)
	{
		if((!mListItems.isEmpty()) && mListItems.size() > position)
		{
			return ((ListItem)mListItems.toArray()[position]).text1;
		}
		
		return null;
	}
	
	/** ================================================ */
	public long getItemId(int position) { return 0; }
	
	/** ================================================ */
	// create a new ImageView for each item referenced by the Adapter
	public View getView(int position, View convertView, ViewGroup parent)
	{
	    View view= (View)convertView;
	     
	    if(null == view)
	    	view= View.inflate(mContext, R.layout.item_text_3, null);
	
	    // view.setLayoutParams(new AbsListView.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));

	    if((!mListItems.isEmpty()) && mListItems.size() > position)
	    {
		    TextView text1	= (TextView)view.findViewById(R.id.it3_text1);
		    TextView text2	= (TextView)view.findViewById(R.id.it3_text2);
		    TextView text3	= (TextView)view.findViewById(R.id.it3_text3);
		    TextView text4	= (TextView)view.findViewById(R.id.it3_text4);
		    TextView text5	= (TextView)view.findViewById(R.id.it3_text5);

	    	ListItem item= (ListItem)mListItems.toArray()[position];

			text1.setText(item.text1);
			text2.setText(item.text2);
			text3.setText(item.text3);
			text4.setText(item.text4+ " dbm");
			text5.setText(item.text5);
		}
	    else
	    {
	    	view.setVisibility(View.GONE);
	    }

	    return view;
	}

	/** ================================================ */
	@Override
    public boolean isEnabled(int position) 
    {
		if(mListItems.size() <= position)
			return false;

        return true;
    }

	/** ================================================ */
	public boolean addItem(ListItem item)
	{
		mListItems.add(item);
	  	return true;
	}
  
	/** ================================================ */
	public void clear()
	{
		mListItems.clear();
	}
	/** ============================================================== */
}